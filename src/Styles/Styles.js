// styleHelpers.js
const layoutHelpers = {
  flex: {display: 'flex'},
  flexRow: {flexDirection: 'row'},
  flexColumn: {flexDirection: 'column'},
  justifyCenter: {justifyContent: 'center'},
  justifyBetween: {justifyContent: 'space-between'},
  justifyAround: {justifyContent: 'space-around'},
  justifyEvenly: {justifyContent: 'space-evenly'},
  alignCenter: {alignItems: 'center'},
  alignStart: {alignItems: 'flex-start'},
  alignEnd: {alignItems: 'flex-end'},
};

const marginHelpers = {
  m1: {margin: 5},
  m2: {margin: 10},
  m3: {margin: 15},
  mt1: {marginTop: 5},
  mr1: {marginRight: 5},
  mb1: {marginBottom: 5},
  ml1: {marginLeft: 5},
  // Add more as needed
};

const paddingHelpers = {
  p1: {padding: 5},
  p2: {padding: 10},
  p3: {padding: 15},
  pt1: {paddingTop: 5},
  pr1: {paddingRight: 5},
  pb1: {paddingBottom: 5},
  pl1: {paddingLeft: 5},
  // Add more as needed
};

const borderHelpers = {
  border: {borderWidth: 1, borderColor: 'black'},
  rounded: {borderRadius: 5},
  // Add more as needed
};

const backgroundColorHelpers = {
  bgPrimary: {backgroundColor: '#007bff'},
  bgSecondary: {backgroundColor: '#6c757d'},
  // Add more color utilities as needed
};

const fontHelpers = {
  textSmall: {fontSize: 12},
  textMedium: {fontSize: 16},
  textLarge: {fontSize: 20},
  textBold: {fontWeight: 'bold'},
  textCenter: {textAlign: 'center'},
  textDark: {color: 'rgba(0,0,0,1)'},
  textUpper: {textTransform: 'uppercase'},
  // Add more as needed
};

const hstyles = {
  ...layoutHelpers,
  ...marginHelpers,
  ...paddingHelpers,
  ...borderHelpers,
  ...backgroundColorHelpers,
  ...fontHelpers,
};

export default hstyles;
