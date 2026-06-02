import csv
import os

input_file = 'app/src/main/res/raw/reglas_clinicas.csv'
output_file = 'app/src/main/res/raw/reglas_clinicas_new.csv'

with open(input_file, 'r', encoding='utf-8') as f_in, open(output_file, 'w', encoding='utf-8', newline='') as f_out:
    reader = csv.reader(f_in)
    writer = csv.writer(f_out)
    
    headers = next(reader)
    writer.writerow(['estado_lecho', 'nivel_exudado', 'infeccion', 'desbridamiento', 'familia_buscada'])
    
    for row in reader:
        if len(row) < 4:
            continue
        lecho, exudado, infeccion, familia_buscada = row[0], row[1], row[2], row[3]
        
        # Row with desbridamiento = false
        # Remove 'Desbridante Enzimatico' from familia_buscada
        familias_false = [f.strip() for f in familia_buscada.split('/') if f.strip() != 'Desbridante Enzimatico']
        familia_buscada_false = '/'.join(familias_false)
        if not familia_buscada_false:
            familia_buscada_false = "No Aplica"
            
        writer.writerow([lecho, exudado, infeccion, 'false', familia_buscada_false])
        
        # Row with desbridamiento = true
        writer.writerow([lecho, exudado, infeccion, 'true', familia_buscada])

os.replace(output_file, input_file)
print("CSV actualizado")
