import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {UvcCamera} from 'react-native-uvc-camera';

const App = () => {
  return (
    <View>
      <Text>Hello World</Text>
      <View>
        <UvcCamera
          ref={this.refCamera}
          style={styles.camera}
          rotation={270}
          // type={UvcCamera.Constants.Type.back}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  camera: {
    width: '100%',
    height: '100%',
  },
});

export default App;
