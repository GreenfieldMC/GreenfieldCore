package net.greenfieldmc.core.greenfieldapi.models.redblocks;

public class GfRedblockProject {

    private String projectName;

    private String projectKey;

    public GfRedblockProject() {
    }

    public GfRedblockProject(String projectName, String projectKey) {
        this.projectName = projectName;
        this.projectKey = projectKey;
    }


    public String getProjectName() {
        return projectName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    @Override
    public String toString() {
        return "GfRedblockProject{" +
                "projectName='" + projectName + '\'' +
                ", projectKey='" + projectKey + '\'' +
                '}';
    }
}
