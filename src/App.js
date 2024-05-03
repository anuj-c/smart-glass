import React, {useEffect, useRef, useState} from 'react';
import {
  Button,
  Text,
  View,
  NativeModules,
  ScrollView,
  Pressable,
} from 'react-native';
import {UvcCamera} from 'react-native-uvc-camera';
import {hstyles, styles} from './Styles';

const {ObjectDetection} = NativeModules;
let refCamera = React.createRef();

const App = () => {
  const [obje, setObje] = useState(['Objects', 'Detected', 'Here']);
  const [view, setView] = useState(false);
  const [detect, setDetect] = useState(false);
  const [showAllBtns, setShowAllBtns] = useState(false);
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

  const handleStop = () => {
    setDetect(false);
    setView(false);
  };

  const joinAfterAs = array => {
    const index = array.indexOf('as');

    if (index !== -1 && index < array.length - 1) {
      return array.slice(index + 1).join(' ');
    }

    return '';
  };

  const isMatch = (resArray, someArr = [], everyArr = []) => {
    if (
      someArr.some(word => resArray.includes(word)) &&
      everyArr.every(word => resArray.includes(word))
    ) {
      return true;
    }
    return false;
  };

  const executedCommand = res => {
    const resArray = res.toLowerCase().split(' ');
    setObje(resArray);
    console.log(resArray);
    if (isMatch(resArray, ['start'])) {
      setView(true);
    } else if (isMatch(resArray, ['stop', 'terminate', 'end'], ['camera'])) {
      handleStop();
    } else if (isMatch(resArray, ['text'])) {
      detectText();
    } else if (isMatch(resArray, ['medicine'])) {
      detectMedicineName();
    } else if (isMatch(resArray, ['expiry', 'date'])) {
      detectExpiry();
    } else if (isMatch(resArray, ['headline', 'headlines'])) {
      detectHeadline();
    } else if (isMatch(resArray, ['object', 'objects'], ['detect'])) {
      detectObjects();
    } else if (isMatch(resArray, ['remember'])) {
      const nameToSave = joinAfterAs(resArray);
      if (nameToSave === '') {
        speakText('Name was unclear. Please say the name again.');
        setTimeout(async () => {
          const justName = await ObjectDetection.callListener();
          handleFaceRecog(justName.toLowerCase());
        }, 2000);
      } else {
        handleFaceRecog(nameToSave.toLowerCase());
      }
    } else if (isMatch(resArray, ['who', 'person'])) {
      handleFaceDetect();
    } else if (isMatch(resArray, ['stop', 'terminate', 'end'], ['speaker'])) {
      stopSpeaking();
    } else if (isMatch(resArray, ['currency', 'money'])) {
      detectCurrency();
    } else if (isMatch(resArray, ['locate'])) {
      locateObject(resArray[resArray.length - 1]);
    } else if (isMatch(resArray, ['is'], ['there'])) {
      findTheObject(resArray[2]);
    } else if (isMatch(resArray, ['many', 'people'])) {
      findNumPeople();
    } else if (isMatch(resArray, ['delete'])) {
      handleFaceDelete(resArray.slice(1).join(' '));
    } else {
      speakText('Command not found, please try again!');
    }
  };

  const findSimilarSound = async words => {
    try {
      const res = await ObjectDetection.findSimilarSound(words);
      console.log(res);
    } catch (err) {
      console.log(err);
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

  const stopRecording = () => {
    refCamera.stopRecording();
  };

  const handleRecord = async () => {
    try {
      const recordPromise = refCamera.recordAsync();

      setTimeout(() => {
        refCamera.stopRecording();
      }, 2000);

      const res = await recordPromise;
      console.log(res.uri);
      const ret = await ObjectDetection.detectObjectFromRecording(res.uri);
      console.log(ret);
      setObje(ret);
    } catch (e) {
      console.log(e);
    }
  };

  const handleFaceRecog = async name => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.saveFace(data.uri, name);
        console.log(res);
        setObje([res]);
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

  const handleFaceDelete = async personName => {
    try {
      const res = await ObjectDetection.deleteFace(personName);
      console.log(res);
    } catch (e) {
      console.log(e);
    }
  };

  const handleFaceDetect = async () => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.detectFaces(data.uri);
        console.log(res);
        setObje([res]);
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

  const detectMedicineName = async () => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.detectMedicineName(data.uri);
        console.log(res);
        setObje([res]);
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

  const detectExpiry = async () => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.detectExpiry(data.uri);
        console.log(res);
        setObje([res]);
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

  const detectHeadline = async () => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.detectHeadline(data.uri);
        console.log(res);
        setObje([res]);
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

  const detectObjects = async () => {
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
  };

  const locateObject = async objectName => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.locateObject(data.uri, objectName);
        console.log(res);
        setObje([res]);
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

  const findTheObject = async objectName => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.findTheObject(data.uri, objectName);
        console.log(res);
        setObje([res]);
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

  const findNumPeople = async () => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const res = await ObjectDetection.findNumPeople(data.uri);
        console.log(res);
        setObje([res]);
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

  const detectCurrency = async () => {
    try {
      const data = await refCamera.takePictureAsync();
      try {
        console.log(data.uri);
        const res = await ObjectDetection.detectCurrency(data.uri);
        console.log(res);
        setObje([res]);
      } catch (e) {
        console.log(e);
      }
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

  const stopSpeaking = async () => {
    try {
      const res = await ObjectDetection.stopSpeaker();
      console.log(res);
    } catch (e) {
      console.log(e);
    }
  };

  return (
    <View style={[styles.container]}>
      <View style={[styles.headerText]}>
        <Text style={[styles.textStyles]}>SmartG</Text>
        <View style={[styles.showAllBtn]}>
          <Button title="Show" onPress={() => setShowAllBtns(prev => !prev)} />
        </View>
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
          </View>

          <View style={[!showAllBtns && styles.functionalityContainer]}>
            {!showAllBtns ? (
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
            ) : (
              <View style={[hstyles.p2, styles.functionalityButtons]}>
                <View style={[hstyles.p2]}>
                  <Button title="Start" onPress={() => setView(true)} />
                </View>
                <View style={[hstyles.p2]}>
                  <Button title="Stop" onPress={stopRecording} />
                </View>
                <View style={[hstyles.p2]}>
                  <Button title="Listen" onPress={handleListen} />
                </View>
                <View style={[hstyles.p2]}>
                  <Button
                    title="Clear"
                    onPress={() => setObje(['Objects', 'Detected', 'Here'])}
                  />
                </View>
                <View style={[hstyles.p2]}>
                  <Button title="Detect" onPress={detectObjects} />
                </View>
                <View style={[hstyles.p2]}>
                  <Button title="Detect2" onPress={() => setDetect(true)} />
                </View>
                <View style={[hstyles.p2]}>
                  <Button title="Text" onPress={() => detectText()} />
                </View>
                {/*<View style={[hstyles.p2]}>
                  <Button title="Record" onPress={handleRecord} />
                </View>*/}
                <View style={[hstyles.p2]}>
                  <Button title="Save Face" onPress={handleFaceRecog} />
                </View>
                <View style={[hstyles.p2]}>
                  <Button title="Det Face" onPress={handleFaceDetect} />
                </View>
                <View style={[hstyles.p2]}>
                  <Button title="Del Face" onPress={handleFaceDelete} />
                </View>
                <View style={[hstyles.p2]}>
                  <Button title="Stop Speak" onPress={stopSpeaking} />
                </View>
                <View style={[hstyles.p2]}>
                  <Button title="Currency" onPress={detectCurrency} />
                </View>
                {/*<View style={[hstyles.p2]}>
                  <Button
                    title="Speak"
                    onPress={() => speakText('May the force, be with you!')}
                  />
                </View>*/}
              </View>
            )}
          </View>
        </View>
      </View>
    </View>
  );
};

export default App;
