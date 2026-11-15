$toolsDir = Join-Path $PSScriptRoot '.local\tools'
$javaHome = Join-Path $toolsDir 'zulu17.66.19-ca-jdk17.0.19-win_x64'
$mavenHome = Join-Path $toolsDir 'apache-maven-3.9.16'
$nodeHome = Join-Path $toolsDir 'node'

foreach ($relativeTool in @(
    (Join-Path $javaHome 'bin\javac.exe'),
    (Join-Path $mavenHome 'bin\mvn.cmd'),
    (Join-Path $nodeHome 'npm.cmd')
)) {
    if (-not (Test-Path -LiteralPath $relativeTool -PathType Leaf)) {
        throw "Missing development tool: $relativeTool"
    }
}

$env:JAVA_HOME = $javaHome
$env:MAVEN_HOME = $mavenHome
$toolPaths = @(
    (Join-Path $javaHome 'bin'),
    (Join-Path $mavenHome 'bin'),
    $nodeHome,
    'C:\Program Files\MySQL\MySQL Server 8.0\bin',
    'D:\IntelliJ IDEA 2026.2.1\bin'
)
if (-not (Get-Command git.exe -ErrorAction SilentlyContinue)) {
    $toolPaths += Join-Path $env:USERPROFILE '.cache\codex-runtimes\codex-primary-runtime\dependencies\native\git\cmd'
}
$existingPaths = $env:Path -split ';' | Where-Object { $_ -and $_ -notin $toolPaths }
$env:Path = ($toolPaths + $existingPaths) -join ';'
