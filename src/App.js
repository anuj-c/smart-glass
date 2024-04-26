import React, {useEffect, useRef, useState} from 'react';
import {
  Button,
  Text,
  View,
  NativeModules,
  ScrollView,
  Pressable,
} from 'react-native';
import {IconButton, MD3Colors} from 'react-native-paper';
import {UvcCamera} from 'react-native-uvc-camera';
import {hstyles, styles} from './Styles';

const {ObjectDetection} = NativeModules;
let refCamera = React.createRef();

const App = () => {
  const [obje, setObje] = useState(['Objects', 'Detected', 'Here']);
  const [view, setView] = useState(false);
  const [detect, setDetect] = useState(false);
  const detectRef = useRef(detect);

  // useEffect to continuously perform object detection
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

  const detectText = async () => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.detectText(data.uri);
        console.log(res);
        setObje([res]);
        speakText(res);
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
    switch (res.toLowerCase()) {
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
        speakText('Command not found, please try again!');
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

  //const stopRecording = () => {
  //refCamera.stopRecording();
  //};

  const handleRecord = async () => {
    try {
      //const recordPromise = refCamera.recordAsync();

      //setTimeout(() => {
      //refCamera.stopRecording();
      //}, 1000);

      //const res = await recordPromise;
      //console.log(res.uri);
      const ret = await ObjectDetection.detectObjectFromRecording(
        'file:///storage/emulated/0/Movies/USBCamera/2024-04-18-01-08-16.mp4',
      );
      console.log(ret);
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
      <View style={[styles.headerText]}>
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
                onCameraReady={() => console.log('Camera is ready')}
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
            <View style={[styles.functionalityButtons]}>
              <View style={[hstyles.p2]}>
                <Button title="Start" onPress={() => setView(true)} />
              </View>
              <View style={[hstyles.p2]}>
                <Button
                  title="Clear"
                  onPress={() => setObje(['Objects', 'Detected', 'Here'])}
                />
              </View>
              <View style={[hstyles.p2]}>
                <Button title="Record" onPress={handleRecord} />
              </View>
              <View style={[hstyles.p2]}>
                {/* <Button title="Stop" onPress={stopRecording} /> */}
                <IconButton
                  icon="camera"
                  iconColor={MD3Colors.error50}
                  size={20}
                  onPress={() => console.log('Pressed')}
                />
              </View>
            </View>
          </View>

          <View style={[styles.functionalityContainer]}>
            <Pressable onPress={handleListen} style={[styles.listenButton]}>
              <Text
                style={[
                  hstyles.textUpper,
                  hstyles.textLarge,
                  hstyles.textBold,
                  hstyles.textCenter,
                  hstyles.textLight,
                ]}>
                Listen
              </Text>
            </Pressable>
            {/* <View style={[hstyles.p2, styles.functionalityButtons]}>
              <View style={[hstyles.p2]}>
                <Button title="Start" onPress={() => setView(true)} />
              </View>
              <View style={[hstyles.p2]}>
                <Button title="Stop" onPress={handleStop} />
              </View>
              <View style={[hstyles.p2]}>
                <Button title="Listen" onPress={handleListen} />
              </View>
              <View style={[hstyles.p2]}>
                <Button
                  title="Speak"
                  onPress={() => speakText('May the force, be with you!')}
                />
              </View>
              <View style={[hstyles.p2]}>
                <Button title="Detect" onPress={() => setDetect(true)} />
              </View>
              <View style={[hstyles.p2]}>
                <Button title="Text" onPress={() => detectText()} />
              </View>
            </View> */}
          </View>
        </View>
      </View>
    </View>
  );
};

export default App;
