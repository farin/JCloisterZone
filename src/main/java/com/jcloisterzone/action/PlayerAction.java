package com.jcloisterzone.action;

import java.awt.Color;
import java.awt.Image;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.jcloisterzone.Player;
import com.jcloisterzone.rmi.RmiProxy;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.grid.GridLayer;

public abstract class PlayerAction<T> implements Comparable<PlayerAction<?>>, 
		ForwardBackwardListener, Iterable<T> {

	@Deprecated
    private final String name;
    protected final Set<T> options = new HashSet<T>();
	
    protected Client client;
    private GridLayer gridLayer;

    public PlayerAction(String name) {
        this.name = name;
    }
    
    
    public abstract void perform(RmiProxy server, T target);
    
    @Override
    public Iterator<T> iterator() {
    	return options.iterator();
    }
    
    public PlayerAction<T> add(T option) {
    	options.add(option);
    	return this;
    }
    
    public PlayerAction<T> addAll(Collection<T> options) {
    	this.options.addAll(options);
    	return this;
    }
    
    public ImmutableSet<T> getOptions() {
    	return ImmutableSet.copyOf(options);
    }
    
    public boolean isEmpty() {
    	return options.isEmpty();
    }

    public String getName() {
        return name;
    }

    public Image getImage(Player player, boolean active) {
        return getImage(player != null && active ? player.getColors().getMeepleColor() : Color.GRAY);
    }

    protected GridLayer createGridLayer() {
        return null;
    }

    /** Called when user select action in action panel */
    public void select() {
        gridLayer = createGridLayer();
        if (gridLayer != null) {
            client.getGridPanel().addLayer(gridLayer);
        }
    }

    /** Called when user deselect action in action panel */
    public void deselect() {
        if (gridLayer != null) {
            client.getGridPanel().clearActionDecorations();
            gridLayer = null;
        }
    }

    /** Called after right mouse click */
    public void forward() {
        client.getControlPanel().getActionPanel().rollAction(1);
    }

    public void backward() {
        client.getControlPanel().getActionPanel().rollAction(-1);
    }

    protected Image getImage(Color color) {
        return client.getFigureTheme().getActionImage(this, color);
    }


    protected int getSortOrder() {
        return 1024;
    }

    @Override
    public int compareTo(PlayerAction<?> o) {
        return getSortOrder() - o.getSortOrder();
    }

    public void setClient(Client client) {
        this.client = client;
    }

}
