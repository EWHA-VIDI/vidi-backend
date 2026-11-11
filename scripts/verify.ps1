param(
    [ValidateSet("quick", "full")]
    [string]$Mode = "quick"
)

$ErrorActionPreference = "Stop"

git diff --check
git diff --cached --check

if ($Mode -eq "quick") {
    .\gradlew.bat ktlintCheck detekt test --console=plain
} else {
    .\gradlew.bat ktlintCheck detekt test build --console=plain
}
