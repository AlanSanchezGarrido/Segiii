package com.example.segiii;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.BuildConfig;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.libraries.navigation.DisplayOptions;
import com.google.android.libraries.navigation.NavigationApi;
import com.google.android.libraries.navigation.NavigationView;
import com.google.android.gms.maps.GoogleMap.CameraPerspective;
import com.google.android.libraries.navigation.ListenableResultFuture;
import com.google.android.libraries.navigation.NavigationApi;
import com.google.android.libraries.navigation.Navigator;
import com.google.android.libraries.navigation.RoutingOptions;
import com.google.android.libraries.navigation.SimulationOptions;
import com.google.android.libraries.navigation.StylingOptions;
import com.google.android.libraries.navigation.SupportNavigationFragment;
import com.google.android.libraries.navigation.Waypoint;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MapaUI extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private Map map;
    private Location location;
    private static final String TAG = MapaUI.class.getSimpleName();
    private Navigator mNavigator;
    private SupportNavigationFragment mNavFragment;
    private RoutingOptions mRoutingOptions;
    private static final LatLng CENTER_HUAUCHINANGO = new LatLng(20.174774, -98.051906);

    // Set fields for requesting location permission.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_ui);

        SupportMapFragment mapFragment;
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Map);
        mapFragment.getMapAsync(this);

        location = new Location(this);
        map = new Map(this);
        //initializeNavigationSdk(CENTER_HUAUCHINANGO);



        /*
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map);
        mapFragment.getMapAsync(this);
        El fragment del mapa y el SupportNavigationFragment hacen que solo se pueda usar el
        mapa de navegación, dejando inutil el normal. Soluciones:
        - Dividir las pantallas (Trabajar con intents al iniciar o cancelar una navegación)
        - Solo utilizar el SupportNavigationFragment (No sé que tantos problemas podría generar)
            --Mejor si dividir las pantallas, el sNav no muestra todos los negocios (que yo sepa)

        Pendientes:
        - Agregar el botón/comando de iniciar la navegación
        - Crear otro metodo o arreglar el de iniciar navegación para que esa por el id de un lugar
        - Controlar los eventos de navegación
        - UI de la navegación
        - Limpiar el código
        - guardar rutas en la base de datos
        - hacer que hable con comandos guardados con la base de datos.
         */







        // Configura el botón flotante para centrar el mapa en la ubicación actual
        FloatingActionButton fab = findViewById(R.id.fab_cener_locartion);
        fab.setOnClickListener(v -> {
            // Verifica si se tiene permiso de ubicación
            if (checkLocationPermission()) {
                // Obtiene la ubicación actual
                location.getDeviceLocation(new Location.LocationCallback() {
                    @Override
                    public void onLocationReceived(LatLng location) {
                        // Centra el mapa en la ubicación
                        map.centerOnLocation(location, true);
                    }
                });
            } else {
                // Solicita permiso de ubicación si no está otorgado
                requestLocationPermission();
            }
        });
        /*
         */


    }

    private void initializeNavigationSdk(LatLng destination) {
        if (ContextCompat.checkSelfPermission(
                this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (!mLocationPermissionGranted) {
            displayMessage(
                    "Error loading Navigation SDK: " + "The user has not granted location permission.");
            return;
        }

        // Get a navigator.
        NavigationApi.getNavigator(
                this,
                new NavigationApi.NavigatorListener() {
                    /**
                     * Sets up the navigation UI when the navigator is ready for use.
                     */
                    @Override
                    public void onNavigatorReady(Navigator navigator) {
                        displayMessage("Navigator ready.");
                        mNavigator = navigator;
                        //mNavFragment =(SupportNavigationFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_fragment);

                        mNavFragment.setTripProgressBarEnabled(true);


                        /*
                        mNavFragment.getMapAsync(
                                new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap map) {
                                        mMap = map;

                                        // Navigate to a place, specified by Place ID.
                                        mRoutingOptions = new RoutingOptions();
                                        mRoutingOptions.travelMode(RoutingOptions.TravelMode.WALKING);
                                        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
                                            @Override
                                            public void onPoiClick(PointOfInterest pointOfInterest) {
                                                //Navegacion por el Id del lugar
                                                //navigateToPlace(pointOfInterest.placeId, mRoutingOptions);
                                                Intent intent = new Intent(MapaUI.this, MainActivity.class);
                                                startActivity(intent);
                                                navigateToPlace(pointOfInterest.latLng, mRoutingOptions);
                                                //navigateToPlace(destination, mRoutingOptions);
                                            }
                                        });
                                    }
                                });

                         */

                        // Set the travel mode (DRIVING, WALKING, CYCLING, or TWO_WHEELER).
                        //mRoutingOptions = new RoutingOptions();
                        //mRoutingOptions.travelMode(RoutingOptions.TravelMode.WALKING);

                    }

                    /**
                     * Handles errors from the Navigation SDK.
                     *
                     * @param errorCode The error code returned by the navigator.
                     */
                    @Override
                    public void onError(@NavigationApi.ErrorCode int errorCode) {
                        switch (errorCode) {
                            case NavigationApi.ErrorCode.NOT_AUTHORIZED:
                                displayMessage(
                                        "Error loading Navigation SDK: Your API key is "
                                                + "invalid or not authorized to use the Navigation SDK.");
                                break;
                            case NavigationApi.ErrorCode.TERMS_NOT_ACCEPTED:
                                displayMessage(
                                        "Error loading Navigation SDK: User did not accept "
                                                + "the Navigation Terms of Use.");
                                break;
                            case NavigationApi.ErrorCode.NETWORK_ERROR:
                                displayMessage("Error loading Navigation SDK: Network error.");
                                break;
                            case NavigationApi.ErrorCode.LOCATION_PERMISSION_MISSING:
                                displayMessage(
                                        "Error loading Navigation SDK: Location permission " + "is missing.");
                                break;
                            default:
                                displayMessage("Error loading Navigation SDK: " + errorCode);
                        }
                    }
                });
    }

    private void navigateToPlace(LatLng latlng, RoutingOptions travelMode) {
        Waypoint destination;
        try {
            //Navegacion por el id del lugar
            //destination = Waypoint.builder().setPlaceIdString(placeId).build();
            //destination = Waypoint.builder().setLatLng(20.174774, -98.051906).build();
            destination = Waypoint.builder().setLatLng(latlng.latitude,latlng.longitude).build();
        } catch (Exception e) {
            displayMessage("Error starting navigation: Place ID is not supported.");
            return;
        }

        // Create a future to await the result of the asynchronous navigator task.
        ListenableResultFuture<Navigator.RouteStatus> pendingRoute =
                mNavigator.setDestination(destination, travelMode);

        // Define the action to perform when the SDK has determined the route.
        pendingRoute.setOnResultListener(
                new ListenableResultFuture.OnResultListener<Navigator.RouteStatus>() {
                    @Override
                    public void onResult(Navigator.RouteStatus code) {
                        switch (code) {
                            case OK:
                                // Hide the toolbar to maximize the navigation UI.
                                if (getActionBar() != null) {
                                    getActionBar().hide();
                                }

                                // Customize the navigation UI.
                                //customizeNavigationUI();
                                // Enable voice audio guidance (through the device speaker).
                                mNavigator.setAudioGuidance(Navigator.AudioGuidance.VOICE_ALERTS_AND_GUIDANCE);


                                // Simulate vehicle progress along the route for demo/debug builds.
                                if (BuildConfig.DEBUG) {
                                    mNavigator
                                            .getSimulator()
                                            .simulateLocationsAlongExistingRoute(
                                                    new SimulationOptions().speedMultiplier(5));
                                }

                                // Start turn-by-turn guidance along the current route.
                                mNavigator.startGuidance();
                                break;
                            // Handle error conditions returned by the navigator.
                            case NO_ROUTE_FOUND:
                                displayMessage("Error starting navigation: No route found.");
                                break;
                            case NETWORK_ERROR:
                                displayMessage("Error starting navigation: Network error.");
                                break;
                            case ROUTE_CANCELED:
                                displayMessage("Error starting navigation: Route canceled.");
                                break;
                            default:
                                displayMessage("Error starting navigation: " + String.valueOf(code));
                        }
                    }
                });
    }


    private void displayMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.d(TAG, errorMessage);
    }

    /** Customizes the navigation UI and the map. */
    private void customizeNavigationUI() {
        // Set custom colors for the navigator.
        mNavFragment.setStylingOptions(
                new StylingOptions()
                        .primaryDayModeThemeColor(0xff1A237E)
                        .secondaryDayModeThemeColor(0xff3F51B5)
                        .primaryNightModeThemeColor(0xff212121)
                        .secondaryNightModeThemeColor(0xff424242)
                        .headerLargeManeuverIconColor(0xffffff00)
                        .headerSmallManeuverIconColor(0xffffa500)
                        .headerNextStepTypefacePath("/system/fonts/NotoSerif-BoldItalic.ttf")
                        .headerNextStepTextColor(0xff00ff00)
                        .headerNextStepTextSize(20f)
                        .headerDistanceTypefacePath("/system/fonts/NotoSerif-Italic.ttf")
                        .headerDistanceValueTextColor(0xff00ff00)
                        .headerDistanceUnitsTextColor(0xff0000ff)
                        .headerDistanceValueTextSize(20f)
                        .headerDistanceUnitsTextSize(18f)
                        .headerInstructionsTypefacePath("/system/fonts/NotoSerif-BoldItalic.ttf")
                        .headerInstructionsTextColor(0xffffff00)
                        .headerInstructionsFirstRowTextSize(24f)
                        .headerInstructionsSecondRowTextSize(20f)
                        .headerGuidanceRecommendedLaneColor(0xffffa500));

        mMap.setTrafficEnabled(true);

        // Place a marker at the final destination.
        if (mNavigator.getCurrentRouteSegment() != null) {
            LatLng destinationLatLng = mNavigator.getCurrentRouteSegment().getDestinationLatLng();

            Bitmap destinationMarkerIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_person);

            mMap.addMarker(
                    new MarkerOptions()
                            .position(destinationLatLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(destinationMarkerIcon))
                            .title("Destination marker"));

            // Listen for a tap on the marker.
            mMap.setOnMarkerClickListener(
                    new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            displayMessage(
                                    "Marker tapped: "
                                            + marker.getTitle()
                                            + ", at location "
                                            + marker.getPosition().latitude
                                            + ", "
                                            + marker.getPosition().longitude);

                            // The event has been handled.
                            return true;
                        }
                    });
        }

        // Set the camera to follow the device location with 'TILTED' driving view.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.followMyLocation(CameraPerspective.TOP_DOWN_HEADING_UP);

        mNavFragment.setTripProgressBarEnabled(true);
        //DisplayOptions displayOptions = new DisplayOptions().showTrafficLights(true).showStopSigns(true);

    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.enableMyLocation();
                location.getDeviceLocation(new Location.LocationCallback() {
                    @Override
                    public void onLocationReceived(LatLng location) {
                        map.centerOnLocation(location, true);
                    }
                });
            } else {
                Toast.makeText(this, "Permiso de la ubicacion denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map.initializeMap(googleMap);
        googleMap.setOnPoiClickListener(MapaUI.this);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER_HUAUCHINANGO, 15f));
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        Toast.makeText(this, "aaaaa", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MapaUI.this, MainActivity.class);
        intent.putExtra("latitude", pointOfInterest.latLng.latitude);
        intent.putExtra("longitude", pointOfInterest.latLng.longitude);
        startActivity(intent);
    }
}