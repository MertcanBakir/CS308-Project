rootProject.name = "backhend"
include("src:test:reports")
findProject(":src:test:reports")?.name = "reports"
