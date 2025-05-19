package com.example.segiii.navigation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.volley.BuildConfig;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.navigation.ArrivalEvent;
import com.google.android.libraries.navigation.ForceNightMode;
import com.google.android.libraries.navigation.ListenableResultFuture;
import com.google.android.libraries.navigation.NavigationApi;
import com.google.android.libraries.navigation.Navigator;
import com.google.android.libraries.navigation.RoutingOptions;
import com.google.android.libraries.navigation.SimulationOptions;
import com.google.android.libraries.navigation.SupportNavigationFragment;
import com.google.android.libraries.navigation.Waypoint;

public class Navigation {

    Context context;
    Activity activity;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    //private static final String TAG = MainActivity.class.getSimpleName();
    private Navigator mNavigator;
    private SupportNavigationFragment mNavFragment;
    private RoutingOptions mRoutingOptions;
    private static final LatLng CENTER_HUAUCHINANGO = new LatLng(20.174774, -98.051906);

    // Set fields for requesting location permission.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    public Navigation(Context context, SupportNavigationFragment mNavFragment, Activity activity, String destination){
        this.mNavFragment = mNavFragment;
        this.activity = activity;
        this.context = context;

        // Get a navigator.
        NavigationApi.getNavigator(
                activity,
                new NavigationApi.NavigatorListener() {
                    /**
                     * Sets up the navigation UI when the navigator is ready for use.
                     */
                    @Override
                    public void onNavigatorReady(Navigator navigator) {
                        displayMessage("Navigator ready.");
                        mNavigator = navigator;

                        //Barra de progreso hacia el destino
                        //mNavFragment.setTripProgressBarEnabled(true);

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mNavFragment.getMapAsync(
                                googleMap -> googleMap.followMyLocation(GoogleMap.CameraPerspective.TILTED));

                        mNavFragment.getMapAsync(
                                new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap map) {
                                        mNavFragment.setForceNightMode(ForceNightMode.FORCE_DAY);
                                        navigateToPlace(destination);
                                    }
                                });

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

    private void navigateToPlace(String placeId) {
        Waypoint destination;
        try {
            destination = Waypoint.builder().setPlaceIdString(placeId).build();
            //destination = Waypoint.builder().setTitle("").build();

        } catch (Exception e) {
            displayMessage("Error starting navigation: Place ID is not supported.");
            return;
        }

        //Definir el tipo de navegación a caminando
        mRoutingOptions = new RoutingOptions();
        mRoutingOptions.travelMode(RoutingOptions.TravelMode.WALKING);

        // Create a future to await the result of the asynchronous navigator task.
        ListenableResultFuture<Navigator.RouteStatus> pendingRoute =
                mNavigator.setDestination(destination, mRoutingOptions);

        // Define the action to perform when the SDK has determined the route.
        pendingRoute.setOnResultListener(
                new ListenableResultFuture.OnResultListener<Navigator.RouteStatus>() {
                    @Override
                    public void onResult(Navigator.RouteStatus code) {
                        switch (code) {
                            case OK:
                                // Hide the toolbar to maximize the navigation UI.
                                if (activity.getActionBar() != null) {
                                    activity.getActionBar().hide();
                                }

                                //eventos de navegación

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



    private void registerNavigationListeners(){
        mNavigator.addArrivalListener(new Navigator.ArrivalListener() {
            @Override
            public void onArrival(ArrivalEvent arrivalEvent) {
                // Start turn-by-turn guidance for the next leg of the route.
                if (arrivalEvent.isFinalDestination()) {
                    displayMessage("onArrival: You've arrived at the final destination.");
                    stopNavigation();
                } else {
                    mNavigator.continueToNextDestination();
                    mNavigator.startGuidance();
                }
            }
        });
    }

    public void stopNavigation(){
        mNavigator.stopGuidance();
        Toast.makeText(context, "Navigator detenido", Toast.LENGTH_SHORT).show();
    }

    private void displayMessage(String errorMessage) {
        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
    }

}
