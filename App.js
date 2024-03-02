/* eslint-disable react-native/no-inline-styles */
import React, {useEffect, useRef, useState} from 'react';
import {
  Button,
  StyleSheet,
  Text,
  View,
  NativeModules,
  Image,
} from 'react-native';
import {UvcCamera} from 'react-native-uvc-camera';

const {ObjectDetection} = NativeModules;
let refCamera = React.createRef();

const App = () => {
  const [obje, setObje] = useState(['hello']);
  const [view, setView] = useState(false);
  const [detect, setDetect] = useState(false);
  const [segment, setSegment] = useState('');
  const detectRef = useRef(detect);
  const [dText, setDText] = useState('');
  // const viewRef = useRef(view);

  // let counterId;

  // useEffect for uploading image to server */
  // useEffect(() => {
  //   viewRef.current = view;
  // }, [view]);

  // const uploadImage = async uri => {
  //   const formData = new FormData();
  //   formData.append('image', {
  //     uri: uri,
  //     type: 'image/jpeg',
  //     name: 'upload.jpg',
  //   });

  //   try {
  //     const response = await fetch('http://10.42.0.1:5000/upload-image', {
  //       method: 'POST',
  //       body: formData,
  //       headers: {
  //         'Content-Type': 'multipart/form-data',
  //       },
  //     });

  //     const responseJson = await response.json();
  //     return responseJson.item;
  //   } catch (error) {
  //     console.error(error);
  //   }
  // };

  // if (view) {
  //   counterId = setInterval(async () => {
  //     await runFunction();
  //   }, 1500);
  // }
  // #endregion */

  useEffect(() => {
    detectRef.current = detect;
    console.log({log: 'inside useEffect', detect});

    const runFunction = async () => {
      while (detectRef.current) {
        console.log({log: 'detecting', detect});
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

  /* #region  Segmentation Function to compute on-device */
  // const segmentFun = async () => {
  //   console.log({log: 'segmenting', detect});
  //   try {
  //     const data = await refCamera.takePictureAsync();
  //     try {
  //       await ObjectDetection.segmentFloor(data.uri);
  //       // const res = await uploadImage(data.uri);
  //       console.log('Finished segment');
  //       // setObje(res);
  //     } catch (e) {
  //       console.log(e);
  //     }
  //   } catch (e) {
  //     console.log(e);
  //   }
  // };
  /* #endregion */

  const segmentFun = async () => {
    console.log({log: 'segmenting', detect});
    try {
      const data = await refCamera.takePictureAsync();
      try {
        const formData = new FormData();
        formData.append('image', {
          uri: data.uri,
          type: 'image/jpeg',
          name: 'upload.jpg',
        });

        try {
          const response = await fetch('http://10.42.0.1:5000/segment-floor', {
            method: 'POST',
            body: formData,
            headers: {
              'Content-Type': 'multipart/form-data',
            },
          });

          const responseJson = await response.json();
          setSegment(responseJson.item);
        } catch (error) {
          console.error(error);
        }
      } catch (e) {
        console.log(e);
      }
    } catch (e) {
      console.log(e);
    }
  };

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
  }

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
  }

  return (
    <View style={[styles.container]}>
      <View style={[styles.text]}>
        <Text style={[styles.textStyles]}>SmartG</Text>
      </View>
      <View style={[{marginBottom: 0}, styles.viewContainer]}>
        {view && (
          <UvcCamera
            ref={ref => {
              refCamera = ref;
            }}
            style={styles.camera}
            rotation={90}
          />
        )}
        {segment.length > 0 && (
          <View style={{zIndex: 100, position: 'absolute'}}>
            <View style={[styles.closeBtn]}>
              <Button title="Close" onPress={() => setSegment('')} />
            </View>
            <Image
              source={{uri: `data:image/jpeg;base64,${segment}`}}
              style={[styles.segImage]}
            />
          </View>
        )}
      </View>
      <View style={[styles.button, {display: 'flex', justifyContent: 'center', alignItems: 'center'}]}>
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
        <View
          style={[
            styles.button,
            {
              display: 'flex',
              justifyContent: 'space-around',
              alignItems: 'center',
              flexDirection: 'row',
            },
         ]}
        >
          <View style={[styles.button]}>
            <Button title="Start" onPress={() => setView(true)} />
          </View>
          <View style={[styles.button]}>
            <Button title="Stop" onPress={handleStop} />
          </View>
          <View style={[styles.button]}>
            <Button title="Save" onPress={saveImage} />
          </View>
        </View>
        <View
          style={[
            styles.button,
            {
              display: 'flex',
              justifyContent: 'space-around',
              alignItems: 'center',
              flexDirection: 'row',
            },
          ]}>
          <View>
            <Button title="Detect" onPress={() => setDetect(true)} />
          </View>
          <View>
            <Button title="Segment" onPress={() => segmentFun()} />
          </View>
          <View>
            <Button title="Text" onPress={() => detectText()} />
          </View>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'white',
  },
  segImage: {
    width: 375,
    height: 280,
    transform: [{rotate: '270deg'}, {translateX: -7}, {translateY: -65}],
  },
  closeBtn: {
    transform: [{rotate: '270deg'}, {translateX: -30}, {translateY: -87}],
    width: 100,
  },
  camera: {
    position: 'relative',
    width: 240,
    height: 370,
    // transform: [{rotate: '90deg'}],
  },
  boundingBox: {
    position: 'absolute',
    borderColor: 'red',
    borderWidth: 2,
    backgroundColor: 'red',
    zIndex: 100,
  },
  viewContainer: {
    top: 0,
    width: 240,
    height: 370,
    marginLeft: 80,
    // backgroundColor: 'black',
    borderColor: 'black',
    borderWidth: 2,
    transform: [{rotate: '90deg'}],
    position: 'relative',
  },
  button: {
    // width: '100%',
    padding: 10,
  },
  text: {
    backgroundColor: 'skyblue',
    marginBottom: 20,
  },
  textStyles: {
    color: 'black',
    fontWeight: 600,
    fontSize: 30,
    textAlign: 'center',
  },
  bottomText: {
    marginBottom: 20,
    textAlign: 'center',
    color: 'black',
    fontWeight: 600,
    fontSize: 20,
    width: '70%',
    borderColor: 'black',
    borderWidth: 2,
    backgroundColor: 'skyblue',
  },
  bottomView: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
  },
});

export default App;
