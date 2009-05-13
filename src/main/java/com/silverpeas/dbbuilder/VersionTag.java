package com.silverpeas.dbbuilder;

/**
 * Titre :        dbBuilder
 * Description :  Builder des BDs Silverpeas
 * Copyright :    Copyright (c) 2001
 * Société :      Stratélia Silverpeas
 * @author ATH
 * @version 1.0
 */

public class VersionTag {

	private String current_or_previous = "";
	private String version = "";

        public VersionTag(String cp, String v) {

		current_or_previous = cp;
		version = v;
        }

	public String getCurrent_or_previous() {
		return current_or_previous;
	}

	public String getVersion() {
		return version;
	}
}
