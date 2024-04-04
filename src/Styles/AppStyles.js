import {StyleSheet} from 'react-native';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'white',
  },
  contentContainer: {
    display: 'flex',
    flex: 1,
    backgroundColor: 'rgba(255,0,0,0.4)',
    justifyContent: 'space-between',
  },
  viewContainer: {
    backgroundColor: 'rgba(0,0,0,0.3)',
    marginTop: 10,
    borderColor: 'black',
    borderWidth: 2,
    position: 'relative',
    display: 'flex',
    alignItems: 'center',
    transform: [{rotate: '90deg'}],
    // width: 240,
    // height: 370,
  },
  camera: {
    position: 'relative',
    width: 240,
    height: 370,
    // transform: [{rotate: '90deg'}],
  },
  functionalityContainer: {
    backgroundColor: 'rgba(255,255,0,0.4)',
  },
  bottomView: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
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
  functionalityButtons: {
    display: 'flex',
    justifyContent: 'space-around',
    alignItems: 'center',
    flexDirection: 'row',
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
  boundingBox: {
    position: 'absolute',
    borderColor: 'red',
    borderWidth: 2,
    backgroundColor: 'red',
    zIndex: 100,
  },
  button: {
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

export default styles;
