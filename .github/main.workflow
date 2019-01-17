workflow "publish" {
  on = "push"
  resolves = ["maven-publish"]
}

action "maven-publish" {
  uses = "./"
  secrets = [
    "GNUPG_KEY",
    "GRADLE_CONFIG",
  ]
}
