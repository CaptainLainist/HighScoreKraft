from flask import Flask, render_template
import yaml
import os

app = Flask(__name__, static_url_path='/static')

# funcion para cargar datos
def cargar_datos():
    try:
        if not os.path.exists('../plugins/logros_puntuacion/points.yml'):
            return []
        
        with open('../plugins/logros_puntuacion/points.yml', 'r') as archivo:
            datos = yaml.safe_load(archivo)
        
        if not datos:
            return []
        
        lista_datos = [{"skin": "caras_skins/" + clave + ".png", "nombre": clave[:-37], "puntuacion": valor} for clave, valor in datos.items()]
        
        lista_datos.sort(key=lambda x: x['puntuacion'], reverse=True)

        return lista_datos
    except Exception as e:
        return []



@app.route('/')
def home():
    # Datos para la tabla
    datos = cargar_datos()
    return render_template('highscore-table.html', datos=datos)

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=1234)