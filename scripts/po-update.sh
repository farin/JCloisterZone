#!/bin/bash

msgmerge -N -U po/ca.po po/keys.pot
msgmerge -N -U po/cs.po po/keys.pot
msgmerge -N -U po/de.po po/keys.pot
msgmerge -N -U po/el.po po/keys.pot
msgmerge -N -U po/en.po po/keys.pot
msgmerge -N -U po/es.po po/keys.pot
msgmerge -N -U po/fi.po po/keys.pot
msgmerge -N -U po/fr.po po/keys.pot
msgmerge -N -U po/hu.po po/keys.pot
msgmerge -N -U po/it.po po/keys.pot
msgmerge -N -U po/nl.po po/keys.pot
msgmerge -N -U po/pl.po po/keys.pot
msgmerge -N -U po/ro.po po/keys.pot
msgmerge -N -U po/ru.po po/keys.pot
msgmerge -N -U po/sk.po po/keys.pot
rm po/*~
rm po/keys.pot