$path = "D:\wallProject\graduation_project\src\main\java\com\wwl\model\entity\User.java"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$content = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
[System.IO.File]::WriteAllText($path, $content, $utf8)
Write-Host "Done - BOM removed, saved as UTF-8 without BOM"
