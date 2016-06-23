package com.communote.server.web.commons.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.common.util.DescendingOrderComparator;

/**
 * Manages a collection of sections with static content which should be displayed by
 * StaticPageVelocityViewController.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class StaticPageContentManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticPageContentManager.class);

    private DescendingOrderComparator orderComperator;
    private List<StaticPageSection> sections;
    private HashSet<String> ids;

    /**
     * Add a new section. The section's order value will be used to position it in the list of all
     * added sections. The higher the value the earlier it will appear in the list. If there is
     * already a section with the same ID the new one will be ignored.
     *
     * @param section
     *            the section to add
     */
    public synchronized void addSection(StaticPageSection section) {
        if (sections == null) {
            this.sections = new ArrayList<>();
            this.sections.add(section);
            this.ids = new HashSet<>();
            this.ids.add(section.getId());
            orderComperator = new DescendingOrderComparator();
        } else {
            if (ids.contains(section.getId())) {
                LOGGER.debug("Ignoring page section because there is already one with ID {}",
                        section.getId());
            } else {
                ArrayList<StaticPageSection> newSections = new ArrayList<>(sections);
                newSections.add(section);
                Collections.sort(newSections, orderComperator);
                this.ids.add(section.getId());
                this.sections = newSections;
                LOGGER.debug("Added section with ID {}", section.getId());
            }
        }
    }

    /**
     * Get all added sections.
     *
     * @return the sections or null if no sections were added
     */
    public List<StaticPageSection> getSections() {
        return sections;
    }

    /**
     * Remove a previously added section from the list.
     *
     * @param id
     *            the ID of the section to remove
     * @return the removed section or null if there is no section with that ID
     */
    public synchronized StaticPageSection removeSection(String id) {
        StaticPageSection removedSection = null;
        if (this.ids != null && this.ids.remove(id)) {
            ArrayList<StaticPageSection> newSections = new ArrayList<>(sections.size() - 1);
            for (StaticPageSection section : sections) {
                if (!section.getId().equals(id)) {
                    newSections.add(section);
                } else {
                    removedSection = section;
                }
            }
            sections = newSections;
            LOGGER.debug("Removed section with ID {}", id);
        }
        return removedSection;
    }

}
