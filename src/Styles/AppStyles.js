import {StyleSheet} from 'react-native';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'white',
  },
  headerText: {
    backgroundColor: 'rgba(135, 206, 235, 1)',
    paddingVertical: 10,
  },
  textStyles: {
    color: 'black',
    fontWeight: 600,
    fontSize: 30,
    textAlign: 'center',
  },
  contentContainer: {
    flex: 1,
  },
  cameraOuterContainer: {
    position: 'relative',
    flexDirection: 'row',
    justifyContent: 'center',
    height: 300,
    backgroundColor: 'rgba(0,0,0,0.4)',
  },
  cameraContainer: {
    position: 'absolute',
    transform: [{rotate: '90deg'}, {translateX: -35}],
  },
  camera: {
    width: 240,
    height: 370,
  },
  resultContainer: {
    flex: 1,
    justifyContent: 'space-between',
  },
  displayResContainer: {
    flex: 1,
    marginVertical: 5,
    backgroundColor: 'rgba(0,150,255,0.4)',
  },
  resultView: {
    marginTop: 10,
    paddingHorizontal: 30,
  },
  resultTextContainer: {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'center',
  },
  resultText: {
    marginBottom: 20,
    marginHorizontal: 10,
    textAlign: 'center',
    color: 'black',
    fontWeight: 500,
    fontSize: 20,
    paddingHorizontal: 10,
    borderColor: 'black',
    borderWidth: 1,
    // backgroundColor: 'skyblue',
    backgroundColor: 'rgba(135,206,235,1)',
  },
  functionalityContainer: {
    backgroundColor: 'rgba(0,150,255,1)',
  },
  listenButton: {
    paddingVertical: 30,
  },
  functionalityButtons: {
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row',
    flexWrap: 'wrap',
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
});

export default styles;
