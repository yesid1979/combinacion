param (
    [string]$SourceFile,
    [string]$TargetFile
)

$ErrorActionPreference = "Stop"

try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $word.DisplayAlerts = "wdAlertsNone"

    $doc = $word.Documents.Open($SourceFile)
    
    # wdFormatPDF = 17
    $doc.SaveAs($TargetFile, 17)
    $doc.Close($false)
    $word.Quit($false)
    
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
} catch {
    Write-Error "Error convirtiendo a PDF: $($_.Exception.Message)"
    if ($word) { $word.Quit($false) }
    exit 1
}
