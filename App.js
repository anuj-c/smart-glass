/* eslint-disable react-native/no-inline-styles */
import React, {useState} from 'react';
import {
  Button,
  Image,
  StyleSheet,
  Text,
  View,
  NativeModules,
} from 'react-native';
import {UvcCamera} from 'react-native-uvc-camera';

const App = () => {
  const [view, setView] = useState(false);
  const [boundingBox, setBoundingBox] = useState({
    bottom: 371,
    left: 375,
    right: 715,
    top: 140,
  });
  const [theuri, setTheuri] = useState(
    'file:///data/user/0/com.smart_g/cache/Camera/d90d36fb-050c-46c4-aaa6-8a94c527eb0b.jpg',
  );

  let refCamera = React.createRef();
  // const [counterId, setCounterId] = useState(null);
  let counterId;

  const {ObjectDetection} = NativeModules;

  if (view) {
    const tempcounterId = setInterval(async () => {
      const data = await refCamera.takePictureAsync();
      await runFunction(data.uri);
    }, 5000);
    // setCounterId(tempcounterId);
    counterId = tempcounterId;
  }

  const runFunction = async (uri) => {
    ObjectDetection.detectObjects(uri).then((res) =>{
      console.log(res);
    }).catch((e) => {
      console.log(e);
    })
    // console.log(res);
    
    // setTheuri(res);
  };

  // useEffect(() => {
  //   const takePhoto = async () => {
  //     if (view) {
  //       const tempcounterId = setInterval(async () => {
  //         const data = await refCamera.takePictureAsync();
  //         console.log(data);
  //       }, 1000);
  //       setCounterId(tempcounterId);
  //     }
  //   };
  //   takePhoto();
  // }, [view]);

  return (
    <View style={[styles.container]}>
      <View style={[styles.text]}>
        <Text style={[styles.textStyles]}>Hello World</Text>
      </View>
      <View style={[{marginBottom: 50}, styles.viewContainer]}>
        {view && (
          <UvcCamera
            ref={ref => {
              refCamera = ref;
            }}
            style={styles.camera}
            rotation={90}
          />
        )}
        <Image
          source={{
            uri: theuri,
          }}
          style={{width: 240, height: 370}}
        />
        <View style={[styles.boundingBox]} />
      </View>
      <View>
        <View style={[styles.button]}>
          <Button title="Start" onPress={() => setView(true)} />
        </View>
        <View style={[styles.button]}>
          <Button
            title="Stop"
            onPress={() => {
              setView(false);
              clearInterval(counterId);
            }}
          />
        </View>
        <View style={[styles.button]}>
          <Button title="Detect" onPress={() => runFunction()} />
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
    width: '100%',
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
});

export default App;
