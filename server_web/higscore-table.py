from flask import Flask, render_template
import yaml

app = Flask(__name__, static_url_path='/static')


def cargar_datos():
    with open('../plugins/logros_puntuacion/points.yml', 'r') as archivo:
        datos = yaml.safe_load(archivo)
    lista_datos = [{"skin": "caras_skins/" + clave + ".png", "nombre": clave[:-37], "puntuacion": valor} for clave, valor in datos.items()]
    
    lista_datos.sort(key=lambda x: x['puntuacion'], reverse=True)

    return lista_datos



@app.route('/')
def home():
    # Datos para la tabla
    datos = cargar_datos()
    return render_template('highscore-table.html', datos=datos)

if __name__ == '__main__':
    app.run(port=1234)