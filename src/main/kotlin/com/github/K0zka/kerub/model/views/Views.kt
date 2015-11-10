package com.github.K0zka.kerub.model.views

/*
 * View definitions for Jackson JSON serialization.
 */

/**
 * Simple view should show only the most vital information on entities, e.g. id and name.
 * Intended use is dropdowns, simple lists.
 */
public open class Simple

/**
 * Detailed view should show the most often used properties of entities beyond the Simple view.
 */
public open class Detailed : Simple()

/**
 * Full view should show all the properties of an entity.
 */
public open class Full : Detailed()