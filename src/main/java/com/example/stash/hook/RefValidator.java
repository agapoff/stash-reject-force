package com.example.stash.hook;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RefValidator implements RepositorySettingsValidator {

    @Override
    public void validate(@Nonnull Settings settings, @Nonnull SettingsValidationErrors settingsValidationErrors, @Nonnull Repository repository) {
        String refIdsRegex = settings.getString("ref-ids-regex");
        if (refIdsRegex != null && !"".equals(refIdsRegex)) {
            try {
                Pattern.compile(refIdsRegex);
            } catch (PatternSyntaxException e) {
                settingsValidationErrors.addFieldError("ref-ids-regex", "The regular expression syntax is invalid.");
            }
        } 
    }
}
