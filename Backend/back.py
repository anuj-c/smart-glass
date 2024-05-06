import shutil
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename
import os
from ultralytics import YOLO
from PIL import Image
import base64

app = Flask(__name__)

model = YOLO("yolov8n.pt")
segModel = YOLO("best.pt")


@app.route('/upload-image', methods=['POST'])
def upload_image():
    if 'image' not in request.files:
        return jsonify({'error': 'No image part in the request'}), 400

    file = request.files['image']
    if file.filename == '':
        return jsonify({'error': 'No image selected for uploading'}), 400

    filename = secure_filename(file.filename)
    temp_path = os.path.join('/tmp', filename)
    file.save(temp_path)

    with Image.open(temp_path) as img:
        rotated_img = img.rotate(270, expand=True)  # Rotate by 270 degrees to get a 90-degree clockwise rotation
        rotated_img.save(temp_path)

    results = model.predict(source=temp_path)

    os.remove(temp_path)

    index = results[0].boxes.cls.cpu()[0].item()

    reqIndex = [63, 39, 41, 56, 59]

    if results[0].boxes.cls.cpu().numpy().size > 0 and index in reqIndex:
      return jsonify({'item': str(results[0].names[results[0].boxes.cls.cpu()[0].item()])}), 200    

    return jsonify({'item': 'no detection'}), 200

@app.route('/segment-floor', methods=['POST'])
def segmentFloor():
    if 'image' not in request.files:
        return jsonify({'error': 'No image part in the request'}), 400

    file = request.files['image']
    if file.filename == '':
        return jsonify({'error': 'No image selected for uploading'}), 400

    filename = secure_filename(file.filename)
    temp_path = os.path.join('/tmp', filename)
    file.save(temp_path)

    with Image.open(temp_path) as img:
        rotated_img = img.rotate(270, expand=True)  # Rotate by 270 degrees to get a 90-degree clockwise rotation
        rotated_img.save(temp_path)

    results = segModel.predict(source=temp_path, save=True)
    print("Done segmenting")

    # processed_image_url = request.url_root + 'runs/segment/predict/' + filename

    with open('runs/segment/predict/' + filename, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf-8')

    os.remove(temp_path)
    shutil.rmtree('runs/segment/predict')
    # os.remove('runs/segment/predict/' + filename)

    return jsonify({'item': encoded_string}), 200

if __name__ == '__main__':
    app.run(host= '0.0.0.0',debug=True, port=5000)
