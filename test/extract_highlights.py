
import zipfile
import re
import sys

def extract_highlighted_text(docx_path, highlight_color="yellow"):
    try:
        with zipfile.ZipFile(docx_path) as docx:
            xml_content = docx.read('word/document.xml').decode('utf-8')
            
            # Simple regex to find highlighted runs
            # Pattern: strict check for <w:highlight w:val="yellow"/> inside <w:rPr>
            # Then extract text in the same run
            
            # Since regex on XML is fragile, we'll iterate through <w:r> blocks
            runs = re.findall(r'<w:r(?: [^>]*)?>(.*?)</w:r>', xml_content, re.DOTALL)
            
            highlighted_texts = []
            
            for run_content in runs:
                if f'<w:highlight w:val="{highlight_color}"' in run_content:
                    # Extract text within this run
                    texts = re.findall(r'<w:t(?: [^>]*)?>(.*?)</w:t>', run_content)
                    if texts:
                        highlighted_texts.append("".join(texts))

            return highlighted_texts

    except Exception as e:
        return [f"Error reading file: {str(e)}"]

if __name__ == "__main__":
    docx_file = r"plantillas/INVERSION_4_COMPLEMENTO_CONTRATO.docx"
    print(f"Extracting yellow highlights from: {docx_file}")
    highlights = extract_highlighted_text(docx_file)
    
    if not highlights:
        print("No yellow highlighted text found.")
    else:
        print("Found the following yellow highlighting:")
        for text in highlights:
            print(f"- {text}")
