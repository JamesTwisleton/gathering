import React, { Component } from 'react';
import {Map, InfoWindow, Marker, GoogleApiWrapper} from 'google-maps-react';

export class MapContainer extends Component {
  render() {
    return (
      <Map google={this.props.google} zoom={14}>

        <Marker onClick={this.onMarkerClick}
                name={'Current location'} />

        {/* <InfoWindow onClose={this.onInfoWindowClose}>
            <div>
              <h1>{this.state.selectedPlace.name}</h1>
            </div>
        </InfoWindow> */}
      </Map>
    );
  }
}

export default GoogleApiWrapper({
//   apiKey: `${process.env.REACT_APP_GOOGLE_API_KEY}`,
    apiKey: 'AIzaSyDu_F54fsRH13VA0-MAzjH3GiJJMj88UJE',
})(MapContainer)


// export default GoogleApiWrapper(
//     (props) => ({
//       apiKey: props.apiKey,
//       language: props.language,
//     }
//   ))(MapContainer)


//   const LoadingContainer = (props) => (
//     <div>Fancy loading container!</div>
//   )
  
//   export default GoogleApiWrapper({
//     apiKey: (`${process.env.REACT_APP_GOOGLE_API_KEY}`),
//     LoadingContainer: LoadingContainer
//   })(MapContainer)
