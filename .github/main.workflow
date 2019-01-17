workflow "New workflow" {
  on = "push"
  resolves = ["maven-publish"]
}

action "maven-publish" {
  uses = "./gradlew"
  runs = "./gradlew"
  args = "build"
}
