/* eslint-disable react-native/no-inline-styles */
import React, {useState} from 'react';
import {Button, StyleSheet, Text, View} from 'react-native';
import {UvcCamera} from 'react-native-uvc-camera';

const App = () => {
  const [view, setView] = useState(false);

  return (
    <View style={[styles.container]}>
      <View style={[styles.text]}>
        <Text style={[styles.textStyles]}>Hello World</Text>
      </View>
      <View style={[{marginBottom: 50}, styles.viewContainer]}>
        {view && (
          <UvcCamera ref={this.refCamera} style={styles.camera} rotation={90} />
        )}
      </View>
      <View style={[styles.button]}>
        <Button title="Take Picture" onPress={() => setView(true)} />
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
    transform: [{rotate: '90deg'}],
  },
  viewContainer: {
    position: 'absolute',
    top: 0,
    marginLeft: 80,
  },
  button: {
    position: 'absolute',
    bottom: 0,
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
