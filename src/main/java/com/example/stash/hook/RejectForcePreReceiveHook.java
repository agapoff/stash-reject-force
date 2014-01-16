package com.example.stash.hook;

import com.atlassian.stash.history.HistoryService;

import com.atlassian.stash.hook.*;
import com.atlassian.stash.hook.repository.*;
import com.atlassian.stash.repository.*;
import java.util.Collection;


import com.atlassian.stash.util.PageRequestImpl;
import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.user.StashAuthenticationContext;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;

public class RejectForcePreReceiveHook implements PreReceiveRepositoryHook {

    private final HistoryService historyService;
    private final StashAuthenticationContext authenticationContext;
    public RejectForcePreReceiveHook(HistoryService historyService, StashAuthenticationContext authenticationContext) {
        //LOG.info("Initializing {}", RejectForcePreReceiveHook.class.getName());
        this.historyService = historyService;
	this.authenticationContext = authenticationContext;
    }

    @Override
    public boolean onReceive(RepositoryHookContext context, Collection<RefChange> refChanges, HookResponse hookResponse) {
        for (RefChange refChange : refChanges)
        {
            List<String> pusherList = Arrays.asList(context.getSettings().getString("force-pushers").split(","));
            //String[] pusherList = context.getSettings().getString("force-pushers").split(",");
            //String pusherList =  context.getSettings().getString("force-pushers");
            if (pusherList.contains(authenticationContext.getCurrentUser().getName())) {
                hookResponse.out().println("Current user " + authenticationContext.getCurrentUser().getName() + " is in force pushers list.");
                return true;
            }
            if (isForcePush(context.getRepository(), refChange) && isRefCoveredByHookSettings(context, refChange.getRefId())) {
                String errorMessage = "Forced pushed to '" + refChange.getRefId() + "' are not allowed by configuration.";
                //LOG.warn(errorMessage);
                hookResponse.err().println(errorMessage);
                return false;
            }

        }
//	hookResponse.out().println(authenticationContext.getCurrentUser().getName());
        return true;
    }

    /**
     * Checks if the given ref change is a forced push by checking the ref change type and asking the history service if the pushed change set already exists in the repository.
     *
     * @param repository
     * @param refChange
     * @return
     */
    boolean isForcePush(Repository repository, RefChange refChange) {
        boolean changeTypeUpdate = refChange.getType() == RefChangeType.UPDATE;
        boolean changeHasServerSideHistory = historyService.getChangesetsBetween(
                repository,
                refChange.getToHash(),
                refChange.getFromHash(),
                new PageRequestImpl(0, 1)
        ).getSize() > 0;
        return changeTypeUpdate && changeHasServerSideHistory;
    }
    /**
     * @param repositoryHookContext
     * @param refName
     * @return
     */
    boolean isRefCoveredByHookSettings(RepositoryHookContext repositoryHookContext, String refName) {
        String refIdsRegexString = repositoryHookContext.getSettings().getString("ref-ids-regex");
        if(refIdsRegexString != null && !"".equals(refIdsRegexString)) {
            Pattern refIdsRegexPattern = Pattern.compile(refIdsRegexString);
            return refIdsRegexPattern.matcher(refName).matches();
        } else {
            return true;
        }
    }


}
