package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.FlockAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.ExprItem;
import com.jcloisterzone.event.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.PointsExpression;
import com.jcloisterzone.event.ScoreEvent.ReceivedPoints;
import com.jcloisterzone.event.TokenPlacedEvent;
import com.jcloisterzone.feature.Field;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Shepherd;
import com.jcloisterzone.game.capability.SheepCapability;
import com.jcloisterzone.game.capability.SheepCapabilityModel;
import com.jcloisterzone.game.capability.SheepToken;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.io.message.FlockMessage;
import com.jcloisterzone.io.message.FlockMessage.FlockOption;
import com.jcloisterzone.random.RandomGenerator;
import com.jcloisterzone.reducers.AddPoints;
import com.jcloisterzone.reducers.UndeployMeeple;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;

import java.util.function.Function;


public class ShepherdPhase extends Phase {


	public ShepherdPhase(RandomGenerator random, Phase defaultNext) {
		super(random, defaultNext);
	}

	private Seq<Field> getClosedFieldsWithShepherd(GameState state) {
		return state.getDeployedMeeples()
				.filterKeys(Predicates.instanceOf(Shepherd.class))
				.values()
				.map(fp -> (Field) state.getFeature(fp))
				.distinct()
				.filter(f -> !f.isOpen(state));
	}

	@Override
	public StepResult enter(GameState state) {
		PlacedTile lastPlaced = state.getLastPlaced();

		Seq<Shepherd> shepherds = state.getTurnPlayer().getSpecialMeeples(state).filter(m -> m instanceof Shepherd).map(m -> (Shepherd) m);
		List<MeeplePointer> unresolvedFlocks = List.empty();
		java.util.Set<Field> unresolvedFields = new java.util.HashSet<>();

		for (var shepherd : shepherds) {
			FeaturePointer shepherdFp = shepherd.getDeployment(state);

			boolean isFieldExtended = false;
			boolean alreadyExpanded = false;

			if (shepherdFp != null) {
				boolean isJustPlaced = lastPlaced.getPosition().equals(shepherdFp.getPosition());

				Field field = (Field) state.getFeature(shepherdFp);
				isFieldExtended = state.getTileFeatures2(lastPlaced.getPosition()).map(Tuple2::_2).contains(field);

				if (isJustPlaced) {
					state = expandFlock(state, shepherdFp);
					alreadyExpanded = true;
				}

				if (!alreadyExpanded && isFieldExtended && !unresolvedFields.contains(field)) {
					unresolvedFlocks = unresolvedFlocks.append(new MeeplePointer(shepherdFp, shepherd.getId()));
					unresolvedFields.add(field);
				}
			}
		}

		for (Field field : getClosedFieldsWithShepherd(state)) {
			if (!unresolvedFields.contains(field)) {
				state = scoreFlock(state, field);
			}
		}

		if (unresolvedFlocks.isEmpty()) {
			return next(state);
		}

		SheepCapability cap = state.getCapabilities().get(SheepCapability.class);
		List<MeeplePointer> _unresolvedFlocks = unresolvedFlocks;
		state = cap.updateModel(state, model -> model.setUnresolvedFlocks(_unresolvedFlocks));
		return prepareAction(state);
	}

	@PhaseMessageHandler
    public StepResult handleFlockMessage(GameState state, FlockMessage msg) {
		SheepCapability cap = state.getCapabilities().get(SheepCapability.class);
		List<MeeplePointer> unresolvedFlocks = cap.getModel(state).getUnresolvedFlocks();

		MeeplePointer mp = unresolvedFlocks.get();
		FeaturePointer shepherdFp = mp.asFeaturePointer();
		var deployment = state.getDeployedMeeples().find(t -> t._1 instanceof Shepherd && t._2.equals(shepherdFp)).get();
		Shepherd shepherd = (Shepherd) deployment._1;

		if (msg.getValue() == FlockOption.EXPAND) {
			state = expandFlock(state, shepherdFp);
			Seq<Field> closedFieldsWithShepherd = getClosedFieldsWithShepherd(state);
			for (Field field : closedFieldsWithShepherd) {
				state = scoreFlock(state, field);
			}
		} else {
			state = scoreFlock(state, shepherdFp);
		}

		state = cap.updateModel(state, model -> model.setUnresolvedFlocks(unresolvedFlocks.pop()));
		return prepareAction(state);
    }

    private StepResult prepareAction(GameState state) {
		SheepCapability cap = state.getCapabilities().get(SheepCapability.class);
		List<MeeplePointer> unresolvedFlocks = cap.getModel(state).getUnresolvedFlocks();

		if (unresolvedFlocks.isEmpty()) {
			return next(state);
		}

		FlockAction action = new FlockAction(unresolvedFlocks.get());
		ActionsState as = new ActionsState(state.getTurnPlayer(), action, false);
		return promote(state.setPlayerActions(as));
	}

	private Map<Meeple, FeaturePointer> getShepherdsOnField(GameState state, Field field) {
		return state.getDeployedMeeples().filter((m, fp) -> m instanceof Shepherd && field.getPlaces().contains(fp));
	}

	private GameState scoreFlock(GameState state, FeaturePointer shepherdFp) {
		Field field = (Field) state.getFeature(shepherdFp);
		state = scoreFlock(state, field);
		Seq<Field> closedFieldsWithShepherd = getClosedFieldsWithShepherd(state);
		for (Field closedField : closedFieldsWithShepherd) {
			state = scoreFlock(state, closedField);
		}
		return clearActions(state);
	}

	private GameState scoreFlock(GameState state, Field field) {
		SheepCapability cap = state.getCapabilities().get(SheepCapability.class);
		SheepCapabilityModel model = cap.getModel(state);
		Map<FeaturePointer, List<SheepToken>> placedTokens = model.getPlacedTokens();
		Map<Meeple, FeaturePointer> shepherdsOnField = getShepherdsOnField(state, field);
		Seq<SheepToken> tokens = shepherdsOnField.values().flatMap(fp -> placedTokens.get(fp).get());
		int points = tokens.map(SheepToken::sheepCount).sum().intValue();
		List<ReceivedPoints> receivedPoints = List.empty();

		List<ExprItem> exprs = tokens.groupBy(Function.identity())
				.toList()
				.sortBy(t -> t._1.ordinal())
				.map(t -> {
					var size = t._2.size();
					return new ExprItem(size, "sheep." + t._1.name(), t._1.sheepCount() * size);
				});
		PointsExpression expr = new PointsExpression("flock", exprs);
		java.util.Set<Player> scoredPlayers = new java.util.HashSet<>(); // when multiple shepherds of one player is placed on the field, then score it only once
		for (Tuple2<Meeple, FeaturePointer> t : shepherdsOnField) {
		    Shepherd shepherd = (Shepherd) t._1;
		    Player player = shepherd.getPlayer();
		    if (!scoredPlayers.contains(player)) {
		    	receivedPoints = receivedPoints.append(new ReceivedPoints(expr, player, shepherd.getDeployment(state)));
				scoredPlayers.add(player);
		    }
            state = (new UndeployMeeple(shepherd, false)).apply(state);
		}
		state = (new AddPoints(receivedPoints, false)).apply(state);

		return cap.setModel(
			state,
			model.setPlacedTokens(shepherdsOnField.values().foldLeft(placedTokens, (acc, fp) -> acc.remove(fp)))
		);
	}

	private GameState expandFlock(GameState state, FeaturePointer shepherdFp) {
		SheepToken drawnToken = drawTokenFromBag(state);

		state = state.appendEvent(new TokenPlacedEvent(PlayEventMeta.createWithoutPlayer(), drawnToken, shepherdFp));

		SheepCapability cap = state.getCapabilities().get(SheepCapability.class);

		if (drawnToken == SheepToken.WOLF) {
			Field field = (Field) state.getFeature(shepherdFp);
			Map<Meeple, FeaturePointer> shepherdsOnField = getShepherdsOnField(state, field);
			for (Meeple m : shepherdsOnField.keySet()) {
				state = (new UndeployMeeple(m, true)).apply(state);
			}

			// remove all placed tokens from capability model
			state = cap.updateModel(state, model -> {
				var placedTokens = model.getPlacedTokens();
				return model.setPlacedTokens(shepherdsOnField.values().foldLeft(placedTokens, (acc, fp) -> acc.remove(fp)));
			});

			return state;
		}

		return state.getCapabilities().get(SheepCapability.class).updateModel(state, model -> {
			var placedTokens = model.getPlacedTokens();
			return model.setPlacedTokens(placedTokens.put(shepherdFp, List.of(drawnToken), (l1, l2) -> l1.appendAll(l2)));
		});
	}

	private SheepToken drawTokenFromBag(GameState state) {
		Vector<SheepToken> bag = state.getCapabilities().get(SheepCapability.class).getBagConent(state);
		return bag.get(getRandom().getNextInt(bag.size()));
	}

}
