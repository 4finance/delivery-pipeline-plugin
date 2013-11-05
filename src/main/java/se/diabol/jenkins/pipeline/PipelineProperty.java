/*
This file is part of Delivery Pipeline Plugin.

Delivery Pipeline Plugin is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Delivery Pipeline Plugin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Delivery Pipeline Plugin.
If not, see <http://www.gnu.org/licenses/>.
*/
package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.Set;

public class PipelineProperty extends JobProperty<AbstractProject<?, ?>> {

    private String taskName = null;
    private String stageName = null;

    public PipelineProperty() {
    }

    public PipelineProperty(String taskName, String stageName) {
        this.taskName = taskName;
        this.stageName = stageName;
    }

    @Exported
    public String getTaskName() {
        return taskName;
    }

    @Exported
    public String getStageName() {
        return stageName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {
        public String getDisplayName() {
            return "Pipeline description";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

        @SuppressWarnings("unused")
        public AutoCompletionCandidates doAutoCompleteStageName(@QueryParameter String value) {
            if (value != null) {
                AutoCompletionCandidates c = new AutoCompletionCandidates();
                Set<String> stages = ProjectUtil.getStageNames();

                for (String stage : stages)
                    if (stage.toLowerCase().startsWith(value.toLowerCase()))
                        c.add(stage);
                return c;
            } else {
                return new AutoCompletionCandidates();
            }
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckStageName(@QueryParameter String value) {
            return checkValue(value);
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckTaskName(@QueryParameter String value) {
            return checkValue(value);
        }

        protected FormValidation checkValue(String value) {
            if (value == null || value.equals("")) {
                return FormValidation.ok();
            }
            if (value.trim().equals("")) {
                return FormValidation.error("Value needs to be empty or include characters and/or numbers");
            }
            return FormValidation.ok();

        }


        @Override
        public PipelineProperty newInstance(StaplerRequest sr, JSONObject formData) throws FormException {
            String taskName = sr.getParameter("taskName");
            String stageName = sr.getParameter("stageName");
            if (taskName != null && taskName.equals("")) {
                taskName = null;
            }
            if (stageName != null && stageName.equals("")) {
                stageName = null;
            }
            if (taskName == null && stageName == null) {
                return null;
            }
            return new PipelineProperty(taskName,
                    stageName);
        }
    }
}
