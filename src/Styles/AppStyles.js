import hstyles from './Styles';
import {StyleSheet} from 'react-native';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'rgba(242, 242, 242, 1)',
  },
  headerText: {
    backgroundColor: 'rgba(30, 30, 30, 1)',
    paddingVertical: 10,
    paddingHorizontal: 20,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 5,
  },
  textStyles: {
    color: 'white',
    fontWeight: 600,
    fontSize: 25,
    textAlign: 'left',
  },
  icon: {
    width: 35,
    height: 35,
  },
  hearIcon: {
    width: 70,
    height: 70,
  },
  contentContainer: {
    flex: 1,
  },
  cameraOuterContainer: {
    position: 'relative',
    flexDirection: 'row',
    justifyContent: 'center',
    height: 300,
    backgroundColor: 'rgba(0,0,0,1)',
  },
  cameraContainer: {
    position: 'absolute',
    transform: [{rotate: '90deg'}, {translateX: -35}],
  },
  cameraContainerText: {
    color: 'white',
    fontWeight: 400,
    fontSize: 20,
    textAlign: 'left',
    position: 'absolute',
    left: 30,
    top: 10,
    zIndex: 10,
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
    // backgroundColor: 'rgba(0,150,255,1)',
  },
  detectionText: {
    ...hstyles.textCenter,
    ...hstyles.textDark,
    ...hstyles.textLarge,
    ...hstyles.textBold,
    ...hstyles.textUpper,
    ...hstyles.m1,
  },
  resultView: {
    marginTop: 10,
    paddingHorizontal: 30,
    paddingBottom: 10,
  },
  resultTextContainer: {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'center',
  },
  resultMapView: {
    marginBottom: 20,
    marginHorizontal: 10,
    paddingHorizontal: 15,
    paddingVertical: 10,
    elevation: 5,
    backgroundColor: 'rgba(255,255,255,1)',
    borderRadius: 10,
  },
  resultText: {
    textAlign: 'center',
    color: 'black',
    fontWeight: 500,
    fontSize: 16,
    ...hstyles.textUpper,
  },
  functionalityContainer: {
    backgroundColor: 'rgba(0,150,255,1)',
  },
  listenButton: {
    height: 200,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    backgroundColor: 'rgba(245, 86, 86,1)',
  },
  listenButtonText: {
    fontSize: 30,
    ...hstyles.textUpper,
    ...hstyles.textBold,
    ...hstyles.textCenter,
    ...hstyles.textLight,
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
  centeredView: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 22,
  },
  modalView: {
    margin: 20,
    backgroundColor: 'white',
    borderRadius: 20,
    padding: 35,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 4,
    elevation: 5,
  },
  modalText: {
    marginBottom: 15,
    textAlign: 'center',
  },
  textInput: {
    height: 40,
    width: 200,
    borderColor: 'gray',
    borderWidth: 1,
    marginBottom: 20,
    color: 'black',
  },
});

export default styles;
