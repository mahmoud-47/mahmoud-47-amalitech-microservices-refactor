# Load .env
Get-Content .env | ForEach-Object {
    if ($_ -and $_ -notmatch '^#') {
        $index = $_.IndexOf('=')
        $key   = $_.Substring(0, $index)
        $value = $_.Substring($index + 1)
        [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
    }
}

# Run Spring Boot
mvn spring-boot:run