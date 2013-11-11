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

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.model.Component;
import se.diabol.jenkins.pipeline.model.Pipeline;
import se.diabol.jenkins.pipeline.sort.ComponentComparator;
import se.diabol.jenkins.pipeline.sort.ComponentComparatorDescriptor;
import se.diabol.jenkins.pipeline.sort.NoOpComparator;
import se.diabol.jenkins.pipeline.util.PipelineUtils;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class DeliveryPipelineView extends View {

    public static final int DEFAULT_INTERVAL = 2;

    private List<ComponentSpec> componentSpecs;
    private int noOfPipelines = 3;
    private boolean showAggregatedPipeline = false;
    private int noOfColumns = 1;
    private String sorting = NoOpComparator.class.getName();
    private String fullScreenCss = null;
    private String embeddedCss = null;
    private boolean showAvatars = false;
    private int updateInterval = DEFAULT_INTERVAL;
    private boolean showChanges = false;

    @DataBoundConstructor
    public DeliveryPipelineView(String name, List<ComponentSpec> componentSpecs) {
        super(name);
        this.componentSpecs = componentSpecs;
    }

    public boolean getShowAvatars() {
        return showAvatars;
    }

    public void setShowAvatars(boolean showAvatars) {
        this.showAvatars = showAvatars;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    public List<ComponentSpec> getComponentSpecs() {
        return componentSpecs;
    }

    public void setComponentSpecs(List<ComponentSpec> componentSpecs) {
        this.componentSpecs = componentSpecs;
    }

    public int getNoOfPipelines() {
        return noOfPipelines;
    }

    public boolean isShowAggregatedPipeline() {
        return showAggregatedPipeline;
    }

    public void setNoOfPipelines(int noOfPipelines) {
        this.noOfPipelines = noOfPipelines;
    }

    public boolean isShowChanges() {
        return showChanges;
    }

    public void setShowChanges(boolean showChanges) {
        this.showChanges = showChanges;
    }

    public void setShowAggregatedPipeline(boolean showAggregatedPipeline) {
        this.showAggregatedPipeline = showAggregatedPipeline;
    }

    public int getNoOfColumns() {
        return noOfColumns;
    }

    public void setNoOfColumns(int noOfColumns) {
        this.noOfColumns = noOfColumns;
    }

    public String getFullScreenCss() {
        return fullScreenCss;
    }

    public int getUpdateInterval() {
        //This occurs when the plugin has been updated and as long as the view has not been updated
        //Jenkins will set the default value to 0
        if (updateInterval == 0) {
            updateInterval = DEFAULT_INTERVAL;
        }

        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public void setFullScreenCss(String fullScreenCss) {
        if (fullScreenCss != null && fullScreenCss.trim().equals("")) {
            this.fullScreenCss = null;
        } else {
            this.fullScreenCss = fullScreenCss;
        }
    }

    public String getEmbeddedCss() {
        return embeddedCss;
    }

    public void setEmbeddedCss(String embeddedCss) {
        if (embeddedCss != null && embeddedCss.trim().equals("")) {
            this.embeddedCss = null;
        } else {
            this.embeddedCss = embeddedCss;
        }
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        Iterator<ComponentSpec> it = componentSpecs.iterator();
        while (it.hasNext()) {
            ComponentSpec componentSpec = it.next();
            if (componentSpec.getFirstJob().equals(oldName)) {
                if (newName == null) {
                    it.remove();
                } else {
                    componentSpec.setFirstJob(newName);
                }
            }
        }
    }

    @Exported
    public String getLastUpdated() {
        return PipelineUtils.formatTimestamp(System.currentTimeMillis());
    }


    @Exported
    public List<Component> getPipelines() {
        List<Component> components = new ArrayList<Component>();
        for (ComponentSpec componentSpec : componentSpecs) {
            AbstractProject firstJob = ProjectUtil.getProject(componentSpec.getFirstJob());
            Pipeline prototype = PipelineFactory.extractPipeline(componentSpec.getName(), firstJob);
            List<Pipeline> pipelines = new ArrayList<Pipeline>();
            if (showAggregatedPipeline)
                pipelines.add(PipelineFactory.createPipelineAggregated(prototype));
            pipelines.addAll(PipelineFactory.createPipelineLatest(prototype, noOfPipelines));
            components.add(new Component(componentSpec.getName(), pipelines));
        }
        if (sorting != null) {
            ComponentComparatorDescriptor comparatorDescriptor = ComponentComparator.all().find(sorting);
            if (comparatorDescriptor != null) {
                Collections.sort(components, comparatorDescriptor.createInstance());
            }
        }

        return components;
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        List<TopLevelItem> result = new ArrayList<TopLevelItem>();
        for (ComponentSpec componentSpec : componentSpecs) {

            AbstractProject project = ProjectUtil.getProject(componentSpec.getFirstJob());
            Collection<AbstractProject<?,?>> projects = ProjectUtil.getAllDownstreamProjects(project).values();
            for (AbstractProject<?, ?> abstractProject : projects) {
                result.add(getItem(abstractProject.getName()));
            }

        }
        return result;
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return getItems().contains(item);
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
        req.bindJSON(this, req.getSubmittedForm());
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return getOwner().getPrimaryView().doCreateItem(req, rsp);
    }


    @Extension
    public static class DescriptorImpl extends ViewDescriptor {
        public ListBoxModel doFillNoOfColumnsItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            options.add("1", "1");
            options.add("2", "2");
            options.add("3", "3");
            return options;
        }

        public ListBoxModel doFillNoOfPipelinesItems(@AncestorInPath ItemGroup<?> context) {
            ListBoxModel options = new ListBoxModel();
            for (int i = 0; i <= 10; i++) {
                String opt = String.valueOf(i);
                options.add(opt, opt);
            }
            return options;
        }

        public ListBoxModel doFillSortingItems() {
            DescriptorExtensionList<ComponentComparator, ComponentComparatorDescriptor> descriptors = ComponentComparator.all();
            ListBoxModel options = new ListBoxModel();
            for (ComponentComparatorDescriptor descriptor : descriptors) {
                options.add(descriptor.getDisplayName(), descriptor.getId());
            }
            return options;
        }

        public FormValidation doCheckUpdateInterval(@QueryParameter String value) {
            int valueAsInt;
            try {
                valueAsInt = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return FormValidation.error("Value must be a integer");
            }
            if (valueAsInt <= 0) {
                return FormValidation.error("Value must be greater that 0");
            }
            return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return "Delivery Pipeline View";
        }
    }


    public static class ComponentSpec extends AbstractDescribableImpl<ComponentSpec> {
        private String name;
        private String firstJob;

        @DataBoundConstructor
        public ComponentSpec(String name, String firstJob) {
            this.name = name;
            this.firstJob = firstJob;
        }

        public String getName() {
            return name;
        }

        public String getFirstJob() {
            return firstJob;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFirstJob(String firstJob) {
            this.firstJob = firstJob;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<ComponentSpec> {

            @Override
            public String getDisplayName() {
                return "";
            }

            public ListBoxModel doFillFirstJobItems(@AncestorInPath ItemGroup<?> context) {
                return ProjectUtil.fillAllProjects(context);
            }

            public FormValidation doCheckName(@QueryParameter String value) {
                if (value != null && !value.trim().equals("")) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Please supply a title!");
                }
            }

        }
    }
}
