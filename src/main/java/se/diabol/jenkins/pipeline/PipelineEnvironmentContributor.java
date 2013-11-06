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
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.util.List;

@Extension
@SuppressWarnings("UnusedDeclaration")
public class PipelineEnvironmentContributor extends RunListener<Run> {

    @Override
    public void onStarted(Run run, TaskListener listener) {
        if (run instanceof AbstractBuild) {
            AbstractBuild build = (AbstractBuild) run;
            AbstractBuild upstreamBuild = PipelineFactory.getUpstreamBuild(build);
            if (upstreamBuild != null) {
                List<ParametersAction> parameters = upstreamBuild.getActions(ParametersAction.class);
                for (ParametersAction parameter : parameters) {
                    ParameterValue value = parameter.getParameter(PipelineVersionContributor.VERSION_PARAMETER);
                    if (value != null && value instanceof StringParameterValue) {
                        String version = ((StringParameterValue) value).value;
                        ParametersAction action = new ParametersAction(
                                new StringParameterValue(PipelineVersionContributor.VERSION_PARAMETER, version));
                        build.addAction(action);

                        listener.getLogger().println("Setting version to: " + version + " from upstream version");

                    }
                }
            }

        }

    }
}
