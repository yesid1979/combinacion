
import csv

file_path = r"c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\doc\MATRIZ PRESTADORES DE SERVICIOS 2026.csv"

try:
    with open(file_path, mode='r', encoding='utf-8', errors='replace') as f:
        # Read the first line manually to see raw content
        line = f.readline()
        print("--- RAW FIRST LINE START ---")
        print(line[:500]) # First 500 chars
        print("--- RAW FIRST LINE END ---")
        
        # Now try csv reader
        f.seek(0)
        reader = csv.reader(f, delimiter=';')
        headers = next(reader)
        print("\n--- DETECTED HEADERS ---")
        for i, h in enumerate(headers):
            print(f"{i}: {h.strip()}")
            
except Exception as e:
    print(f"Error: {e}")
