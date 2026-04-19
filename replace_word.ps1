$ErrorActionPreference = "Stop"

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = "wdAlertsNone"

$baseDir = "C:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion"
$plantillasDir = Join-Path $baseDir "plantillas"

# Ensure plantillas directory exists (it should, but just in case)
if (-not (Test-Path -Path $plantillasDir)) {
    New-Item -ItemType Directory -Force -Path $plantillasDir
}

$files = @(
    "MAJA01.04.03.P003.F001 - JUSTIFICACIÓN PARA LA MODIFICACIÓN DE CONTRATOS ajustado (3).docx",
    "MAJA01.04.03.P003.F002 -MODIFICACION DE CONTRATO - CONVENIO - ACEPTACIÓN DE OFERTA AJUSTADO (1).docx"
)

# Replacements array (Find, Replace) - using wildcard or exact matches for word COM 
# Note: we will replace exact text matches

$replacements = @(
    @("«INCLUIR EL NOMBRE DEL ORGANISMO»", "{{ORGANISMO_ORDENADOR}}"),
    @("«Incluir el nombre o razón social del contratista»", "{{CONTRATISTA_NOMBRE}}"),
    @("«Incluir el número de identificación del Contratista»", "{{CONTRATISTA_CEDULA}}"),
    @("«Incluir el número del Contrato / Orden de Compra / Convenio»", "{{NUMERO_CONTRATO}}"),
    @("«Incluir el objeto del del Contrato / Orden de Compra / Convenio»", "{{OBJETO_CONTRACTUAL}}"),
    @("«Incluir el valor inicial pactado del Contrato / Orden de Compra / Convenio en letras y números»", "{{VALOR_CONTRATO_LETRAS}} mcte. ({{VALOR_CONTRATO}})"),
    @("«Incluir el valor total del contrato a la fecha de suscripción del presente documento»", "{{VALOR_CONTRATO_MAS_ADICION_LETRAS}} mcte. ({{VALOR_CONTRATO_MAS_ADICION}})"),
    @("«Incluir la fecha de inicio (Acta de inicio) del Contrato / Orden de Compra / Convenio»", "{{FECHA_ACTA_INICIO}}"),
    @("«Incluir la fecha de terminación a la fecha de suscripción del presente documento»", "{{FECHA_FIN_CONTRATO}}"),
    @("«Incluir la fecha de terminación del Contrato / Orden de Compra / Convenio»", "{{FECHA_FIN_CONTRATO}}"),
    @("«Incluir la fecha en que se suscribe el Contrato / Orden de Compra / Convenio»", "{{FECHA_SUSCRIPCION}}"),
    @("«Incluir si existen adiciones en letras y números»", "{{VALOR_TOTAL_ADICION_LETRAS}} ({{VALOR_TOTAL_ADICION}})"),
    
    @("«NOMBRE_DEL_CONTRATISTA»", "{{CONTRATISTA_NOMBRE}}"),
    @("«Cedula_de_ciudadania_»", "{{CONTRATISTA_CEDULA}}"),
    @("«No_Contrato, convenio o aceptación de oferta»", "{{NUMERO_CONTRATO}}"),
    @("«No_Contrato»", "{{NUMERO_CONTRATO}}"),
    @("«Fecha_Suscripción_de_contrato_»", "{{FECHA_SUSCRIPCION}}"),
    @("«fecha_acta_de_inicio_»", "{{FECHA_ACTA_INICIO}}"),
    @("«fecha_terminación_»", "{{FECHA_FIN_CONTRATO}}"),
    @("«Valor_de_la_adicion_letra_»", "{{VALOR_TOTAL_ADICION_LETRAS}}"),
    @("«Valor_de_la_adicion_numero__»", "{{VALOR_TOTAL_ADICION}}"),
    @("«objeto»", "{{OBJETO_CONTRACTUAL}}")
)

try {
    foreach ($file in $files) {
        $sourcePath = Join-Path $baseDir "doc\$file"
        $destFile = $file -replace " ", "_"
        # Simplify the filename for the plantillas folder
        if ($destFile -match "F001") {
            $destFile = "MODIFICACION_1_JUSTIFICACION.docx"
        } elseif ($destFile -match "F002") {
            $destFile = "MODIFICACION_2_ACEPTACION.docx"
        }
        $destPath = Join-Path $plantillasDir $destFile
        
        Write-Host "Procesando $file -> $destFile"
        
        $doc = $word.Documents.Open([ref]$sourcePath)
        
        foreach ($rep in $replacements) {
            $findText = $rep[0]
            $replaceText = $rep[1]
            
            $wdReplaceAll = 2
            $matchCase = $false
            $matchWholeWord = $false
            $matchWildcards = $false
            $matchSoundsLike = $false
            $matchAllWordForms = $false
            $forward = $true
            $wrap = 1 # wdFindContinue
            $format = $false
            
            $doc.Content.Find.Execute(
                [ref]$findText, [ref]$matchCase, [ref]$matchWholeWord, 
                [ref]$matchWildcards, [ref]$matchSoundsLike, [ref]$matchAllWordForms, 
                [ref]$forward, [ref]$wrap, [ref]$format, [ref]$replaceText, [ref]$wdReplaceAll
            ) | Out-Null
        }
        
        $doc.SaveAs([ref]$destPath)
        $doc.Close([ref]$false)
        Write-Host "Guardado como $destFile en plantillas/"
    }
} finally {
    $word.Quit([ref]$false)
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
    Remove-Variable word
    Write-Host "Proceso completado."
}
