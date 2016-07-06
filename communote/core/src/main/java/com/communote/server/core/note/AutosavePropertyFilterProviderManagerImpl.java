package com.communote.server.core.note;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.communote.server.api.core.note.AutosavePropertyFilterProvider;
import com.communote.server.api.core.note.AutosavePropertyFilterProviderManager;
import com.communote.server.api.core.property.StringPropertyFilter;
import com.communote.server.api.core.property.StringPropertyTO;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Service("autosavePropertyFilterProviderManager")
public class AutosavePropertyFilterProviderManagerImpl implements
AutosavePropertyFilterProviderManager {
    private List<AutosavePropertyFilterProvider> providers = new ArrayList<>();

    @Override
    public synchronized void addProvider(AutosavePropertyFilterProvider provider) {
        ArrayList<AutosavePropertyFilterProvider> newProviders = new ArrayList<>(providers);
        newProviders.add(provider);
        providers = newProviders;
    }

    @Override
    public Collection<StringPropertyFilter> getFiltersForComment(Long parentNoteId,
            Collection<StringPropertyTO> properties) {
        properties = normalizeProperties(properties);
        ArrayList<StringPropertyFilter> allFilters = new ArrayList<>();
        for (AutosavePropertyFilterProvider provider : providers) {
            Collection<StringPropertyFilter> filters = provider.getFiltersForComment(parentNoteId,
                    properties);
            if (filters != null) {
                allFilters.addAll(filters);
            }
        }
        return allFilters;
    }

    @Override
    public Collection<StringPropertyFilter> getFiltersForCreate(
            Collection<StringPropertyTO> properties) {
        properties = normalizeProperties(properties);
        ArrayList<StringPropertyFilter> allFilters = new ArrayList<>();
        for (AutosavePropertyFilterProvider provider : providers) {
            Collection<StringPropertyFilter> filters = provider.getFiltersForCreate(properties);
            if (filters != null) {
                allFilters.addAll(filters);
            }
        }
        return allFilters;
    }

    @Override
    public Collection<StringPropertyFilter> getFiltersForUpdate(Long noteId,
            Collection<StringPropertyTO> properties) {
        properties = normalizeProperties(properties);
        ArrayList<StringPropertyFilter> allFilters = new ArrayList<>();
        for (AutosavePropertyFilterProvider provider : providers) {
            Collection<StringPropertyFilter> filters = provider.getFiltersForUpdate(noteId,
                    properties);
            if (filters != null) {
                allFilters.addAll(filters);
            }
        }
        return allFilters;
    }

    @PostConstruct
    private void init() {
        providers.add(new RepostAutosavePropertyFilterProvider());
    }

    private Collection<StringPropertyTO> normalizeProperties(Collection<StringPropertyTO> properties) {
        if (properties == null) {
            return Collections.emptyList();
        }
        return properties;
    }

    @Override
    public synchronized void removeProvider(AutosavePropertyFilterProvider provider) {
        if (providers.contains(provider)) {
            ArrayList<AutosavePropertyFilterProvider> newProviders = new ArrayList<>(providers);
            newProviders.remove(provider);
            providers = newProviders;
        }
    }

}
