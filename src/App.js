import React, {useEffect, useRef, useState} from 'react';
import {Button, Text, View, NativeModules, ScrollView} from 'react-native';
import {UvcCamera} from 'react-native-uvc-camera';
import {hstyles, styles} from './Styles';

const {ObjectDetection} = NativeModules;
let refCamera = React.createRef();

const App = () => {
  const [obje, setObje] = useState(['Objects', 'Detected', 'Here']);
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

  /* #region  Save Image function */
  // const saveImage = async () => {
  //   console.log({log: 'saving', detect});
  //   try {
  //     const data = await refCamera.takePictureAsync();
  //     try {
  //       await ObjectDetection.saveImageFromUri(data.uri, 'uploadedImage.jpg');
  //     } catch (e) {
  //       console.log(e);
  //     }
  //   } catch (e) {
  //     console.log(e);
  //   }
  // };
  /* #endregion */

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

  const executedCommand = res => {
    switch (res) {
      case 'start camera':
        setView(true);
        break;
      case 'stop camera':
        handleStop();
        break;
      case 'detect objects':
        setDetect(true);
        break;
      case 'detect text':
        detectText();
        break;
      default:
        console.log('No command found');
    }
  };

  const handleListen = async () => {
    try {
      const res = await ObjectDetection.callListener();
      executedCommand(res);
      console.log(res);
    } catch (e) {
      console.log(e);
    }
  };

  const speakText = async text => {
    try {
      const res = await ObjectDetection.callSpeaker(text);
      console.log(res);
    } catch (e) {
      console.log(e);
    }
  };

  return (
    <View style={[styles.container]}>
      <View style={[styles.text]}>
        <Text style={[styles.textStyles]}>SmartG</Text>
      </View>

      <View style={[styles.contentContainer]}>
        <View style={[styles.cameraOuterContainer]}>
          {view && (
            <View style={[styles.cameraContainer]}>
              <UvcCamera
                ref={ref => {
                  refCamera = ref;
                }}
                style={styles.camera}
                rotation={90}
              />
            </View>
          )}
        </View>

        <View style={[styles.resultContainer]}>
          <View style={[styles.displayResContainer]}>
            <Text
              style={[
                hstyles.textCenter,
                hstyles.textDark,
                hstyles.textLarge,
                hstyles.textBold,
                hstyles.textUpper,
                hstyles.m1,
              ]}>
              Detection
            </Text>
            <ScrollView contentContainerStyle={[styles.resultView]}>
              <View style={[styles.resultTextContainer]}>
                {obje.map((obj, index) => (
                  <Text key={index} style={[styles.resultText]}>
                    {obj}
                  </Text>
                ))}
              </View>
            </ScrollView>
            <View
              style={[
                hstyles.p2,
                hstyles.flex,
                hstyles.alignCenter,
                hstyles.justifyCenter,
              ]}>
              <Button
                title="Clear"
                onPress={() => setObje(['Objects', 'Detected', 'Here'])}
              />
            </View>
          </View>

          <View style={[styles.functionalityContainer]}>
            <View style={[styles.button, styles.functionalityButtons]}>
              <View style={[styles.button]}>
                <Button title="Start" onPress={() => setView(true)} />
              </View>
              <View style={[styles.button]}>
                <Button title="Stop" onPress={handleStop} />
              </View>
              {/* <View style={[styles.button]}>
                <Button title="Save" onPress={saveImage} />
              </View> */}
              <View style={[styles.button]}>
                <Button title="Listen" onPress={handleListen} />
              </View>
              <View style={[styles.button]}>
                <Button
                  title="Speak"
                  onPress={() => speakText('May the force, be with you!')}
                />
              </View>
              <View style={[styles.button]}>
                <Button title="Detect" onPress={() => setDetect(true)} />
              </View>
              <View style={[styles.button]}>
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
