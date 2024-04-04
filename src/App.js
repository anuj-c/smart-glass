import React, {useEffect, useRef, useState} from 'react';
import {Button, Text, View, NativeModules} from 'react-native';
import {UvcCamera} from 'react-native-uvc-camera';
import {hstyles, styles} from './Styles';

const {ObjectDetection} = NativeModules;
let refCamera = React.createRef();

const App = () => {
  const [obje, setObje] = useState(['hello']);
  const [view, setView] = useState(false);
  const [detect, setDetect] = useState(false);
  const detectRef = useRef(detect);

  useEffect(() => {
    detectRef.current = detect;

    const runFunction = async () => {
      while (detectRef.current) {
        try {
          const data = await refCamera.takePictureAsync();
          try {
            const res = await ObjectDetection.detectObjects(data.uri);
            // const res = await uploadImage(data.uri);
            console.log(res);
            setObje(res);
          } catch (e) {
            console.log(e);
          }
        } catch (e) {
          console.log(e);
        }
        await new Promise(resolve => setTimeout(resolve, 500));
      }
    };

    runFunction();
  }, [detect]);

  const saveImage = async () => {
    console.log({log: 'saving', detect});
    try {
      const data = await refCamera.takePictureAsync();
      try {
        await ObjectDetection.saveImageFromUri(data.uri, 'uploadedImage.jpg');
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

  const detectText = async () => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.detectText(data.uri);
        console.log(res);
        setObje([res]);
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

  const handleStop = () => {
    setDetect(false);
    setView(false);
  };

  return (
    <View style={[styles.container]}>
      <View style={[styles.text]}>
        <Text style={[styles.textStyles]}>SmartG</Text>
      </View>

      <View style={[styles.contentContainer]}>
        <View style={[styles.viewContainer]}>
          {view && (
            <UvcCamera
              ref={ref => {
                refCamera = ref;
              }}
              style={styles.camera}
              rotation={90}
            />
          )}
        </View>

        <View style={[styles.functionalityContainer]}>
          <View
            style={[
              styles.button,
              hstyles.flex,
              hstyles.alignCenter,
              hstyles.justifyCenter,
            ]}>
            <Button title="Clear" onPress={() => setObje([])} />
          </View>
          <View style={[styles.bottomView]}>
            {obje.map((obj, index) => (
              <Text key={index} style={[styles.bottomText]}>
                {obj}
              </Text>
            ))}
          </View>
          <View>
            <View style={[styles.button, styles.functionalityButtons]}>
              <View style={[styles.button]}>
                <Button title="Start" onPress={() => setView(true)} />
              </View>
              <View style={[styles.button]}>
                <Button title="Stop" onPress={handleStop} />
              </View>
              <View style={[styles.button]}>
                <Button title="Save" onPress={saveImage} />
              </View>
              <View style={[styles.button]}>
                <Button title="Listen" onPress={saveImage} />
              </View>
              <View style={[styles.button]}>
                <Button title="Speak" onPress={saveImage} />
              </View>
            </View>
            <View
              style={[
                styles.button,
                hstyles.flex,
                hstyles.justifyAround,
                hstyles.alignCenter,
                hstyles.flexRow,
              ]}>
              <View>
                <Button title="Detect" onPress={() => setDetect(true)} />
              </View>
              <View>
                <Button title="Text" onPress={() => detectText()} />
              </View>
            </View>
          </View>
        </View>
      </View>
    </View>
  );
};

export default App;
