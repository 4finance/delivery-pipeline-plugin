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
package se.diabol.jenkins.pipeline.model;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

import static com.google.common.base.Objects.toStringHelper;

@ExportedBean(defaultVisibility = 100)
public class Pipeline extends AbstractItem
{
    private List<Stage> stages;

    private String version;

    private List<UserInfo> triggeredBy;

    private boolean aggregated;

    private String timestamp;

    public Pipeline(String name, String version, String timestamp,  List<UserInfo> triggeredBy, List<Stage> stages, boolean aggregated)
    {
        super(name);
        this.version = version;
        this.triggeredBy = triggeredBy;
        this.aggregated = aggregated;
        this.stages = ImmutableList.copyOf(stages);
        this.timestamp = timestamp;
    }

    @Exported
    public List<Stage> getStages()
    {
        return stages;
    }

    @Exported
    public String getVersion()
    {
        return version;
    }

    @Exported
    @SuppressWarnings("unused")
    public String getTimestamp() {
        return timestamp;
    }

    @Exported
    @SuppressWarnings("unused")
    public boolean isAggregated()
    {
        return aggregated;
    }

    @Exported
    @SuppressWarnings("unused")
    public List<UserInfo> getTriggeredBy() {
        return triggeredBy;
    }

    @Exported
    @SuppressWarnings("unused")
    public int getId() {
        return hashCode();
    }


    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().appendSuper( super.hashCode() ).append( version ).append( stages ).toHashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return o == this || o instanceof Pipeline && equals((Pipeline) o);
    }

    private boolean equals(Pipeline o)
    {
        return super.equals(o) && new EqualsBuilder().appendSuper( super.equals( o ) ).append( stages, o.stages ).append( version, o.version ).isEquals();
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("name", getName())
                .add("version", getVersion())
                .add("stages", getStages())
                .toString();
    }
}
