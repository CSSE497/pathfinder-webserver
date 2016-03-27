String.prototype.hashCode = function(){
    var hash = 0;
    if (this.length == 0) return hash;
    for (i = 0; i < this.length; i++) {
        char = this.charCodeAt(i);
        hash = ((hash<<5)-hash)+char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
};

$(function() {
    if (!Array.prototype.last) {
        Array.prototype.last = function() {
            return this[this.length - 1];
        };
    };

    function tree(cluster) {
        return {
            text: cluster.id.split("/").last(),
            state: { expanded: true },
            nodes: cluster.subclusters.map(tree)
        };
    }

    function subclusterId(dataNodeId) {
        var node = $("#clustertree").treeview("getNode", dataNodeId);
        if (node.parentId != undefined) {
            return subclusterId(node.parentId) + "/" + node.text;
        } else {
            return "/" + node.text;
        }
    }

    function currentSubclusterId() {
        var selected = $("#clustertree").treeview('getSelected')[0];
        return subclusterId(selected.nodeId);
    }

    $("#createsubcluster").prop("disabled", true);
    $("#forcerefresh").prop("disabled", true);
    $("#subclusterinput").keyup(function() {
        $("#createsubcluster").prop("disabled", this.value == "" ? true : false);
    });

    function initializeMap() {
        var mapCanvas = document.getElementById("map");
        var mapOptions = {
            // Initially center on SF
            center: new google.maps.LatLng(37.7833, -122.4167),
            zoom: 11,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            disableDefaultUI: true,
            zoomControl: true
        };
        var map = new google.maps.Map(mapCanvas, mapOptions);
        var directionsService = new google.maps.DirectionsService();

        var renderers = new Map();
        function drawRoutes(routes) {
            var newRenderers = new Map();
            console.log("The routes are");
            console.log(routes);
            for (var i = 0; i < routes.length; i++) {
                var route = routes[i];
                console.log("My route is");
                console.log(route);
                if (route.actions.length < 2) continue;
                var rendererKey = JSON.stringify(route).hashCode();
                if (!renderers.has(rendererKey)) {
                    console.log("Creating new renderer with key " + rendererKey);
                    var directionsDisplay = new google.maps.DirectionsRenderer({
                        preserveViewport: true,
                        suppressMarkers: true
                    });
                    newRenderers.set(rendererKey, directionsDisplay);
                    directionsDisplay.setMap(map);
                    var start = route.actions[0];
                    var end = route.actions[route.actions.length - 1];
                    var request = {
                        origin: {lat: start.latitude, lng: start.longitude},
                        destination: {lat: end.latitude, lng: end.longitude},
                        waypoints: route.actions.slice(1, -1).map(function (a) {
                            return {location: {lat: a.latitude, lng: a.longitude}}
                        }),
                        travelMode: google.maps.TravelMode.DRIVING
                    };
                    console.log(request);
                    directionsService.route(request, (function(display) {
                        // I wrapped this in a closure b/c directionsDisplay changes.
                        return function(result, status) {
                            if (status == google.maps.DirectionsStatus.OK) {
                                display.setDirections(result);
                            } else {
                                console.log("Not great response from gmaps: " + status);
                            }
                        }
                    })(directionsDisplay));
                } else {
                    console.log("Using old renderer");
                    newRenderers.set(rendererKey, renderers.get(rendererKey));
                }
            }
            renderers.forEach(function(v, k, m) { if (!newRenderers.has(k)) v.setMap(null); });
            renderers = newRenderers;
        }

        var markers = new Map();
        var displaycluster = undefined;

        function drawRouteActions(routes) {
            var newMarkers = new Map();
            console.log("Drawing route actions");
            console.log(routes);
            var vehicles = routes.map(function(x) { return x.vehicle; });
            var actions = [].concat.apply([], routes.map(function(x) { return x.actions; }));
            var maxLat = -90
                ,minLat = 90
                ,maxLng = -180
                ,minLng = 180;
            actions.forEach(function(action) {
                maxLat = Math.max(maxLat, action.latitude);
                minLat = Math.min(minLat, action.latitude);
                maxLng = Math.max(maxLng, action.longitude);
                minLng = Math.min(minLng, action.longitude);
                var label = action.action == "Start" ? "T" : action.action == "PickUp" ? "P" : "D";
                var markerKey = JSON.stringify(action);
                if (markers.has(markerKey)) {
                    newMarkers.set(markerKey, markers.get(markerKey));
                } else {
                    newMarkers.set(markerKey, new google.maps.Marker({
                        position: { lat: action.latitude, lng: action.longitude },
                        map: map,
                        label: label
                    }));
                }
            });
            markers.forEach(function(v, k, m) { if (!newMarkers.has(k)) v.setMap(null); });
            markers = newMarkers;
            if (maxLng < minLng) return;
            var minDiff = 0.01;
            var lngDiff = maxLng - minLng;
            var latDiff = maxLat - minLat;
            if (lngDiff < minDiff) {
                maxLng = maxLng + (minDiff - lngDiff) / 2;
                minLng = minLng - (minDiff - lngDiff) / 2;
            }
            if (latDiff < minDiff) {
                maxLat = maxLat + (minDiff - latDiff) / 2;
                minLat = minLat - (minDiff - latDiff) / 2;
            }
            var bounds = {
                "east": maxLng,
                "west": minLng,
                "north": maxLat,
                "south": minLat
            };
            map.fitBounds(bounds);
            map.panToBounds(bounds);
        }

        function updateMap(path) {
            $("#maplabel").text("Cluster id: " + path);
            if (displaycluster) {
                displaycluster.routeUnsubscribe();
            }
            pf.getCluster(path, function(cluster) {
                markers.forEach(function(v, k, m) { v.setMap(null); });
                markers = new Map();
                displaycluster = cluster;
                var maxLat = -90
                    ,minLat = 90
                    ,maxLng = -180
                    ,minLng = 180;
                cluster.commodities.forEach(function(commodity) {
                    if (commodity.status == "Waiting") {
                        maxLat = Math.max(maxLat, commodity.startLat, commodity.endLat);
                        minLat = Math.min(minLat, commodity.startLat, commodity.endLat);
                        maxLng = Math.max(maxLng, commodity.startLng, commodity.endLng);
                        minLng = Math.min(minLng, commodity.startLng, commodity.endLng);
                        markers.set(JSON.stringify(commodity) + "P", new google.maps.Marker({
                            position: { lat: commodity.startLat, lng: commodity.startLng },
                            map: map,
                            label: "P"
                        }));
                        markers.set(JSON.stringify(commodity) + "D", new google.maps.Marker({
                            position: { lat: commodity.endLat, lng: commodity.endLng },
                            map: map,
                            label: "D"
                        }));
                    }
                });
                cluster.transports.forEach(function(transport) {
                    if (transport.status == "Online") {
                        maxLat = Math.max(maxLat, transport.lat);
                        minLat = Math.min(minLat, transport.lat);
                        maxLng = Math.max(maxLng, transport.lng);
                        minLng = Math.min(minLng, transport.lng);
                        markers.set(JSON.stringify(transport), new google.maps.Marker({
                            position: { lat: transport.lat, lng: transport.lng },
                            map: map,
                            label: "T"
                        }));
                    }
                });
                if (maxLng < minLng) return;
                var minDiff = 0.01;
                var lngDiff = maxLng - minLng;
                var latDiff = maxLat - minLat;
                if (lngDiff < minDiff) {
                    maxLng = maxLng + (minDiff - lngDiff) / 2;
                    minLng = minLng - (minDiff - lngDiff) / 2;
                }
                if (latDiff < minDiff) {
                    maxLat = maxLat + (minDiff - latDiff) / 2;
                    minLat = minLat - (minDiff - latDiff) / 2;
                }
                var bounds = {
                    "east": maxLng,
                    "west": minLng,
                    "north": maxLat,
                    "south": minLat
                };
                map.fitBounds(bounds);
                map.panToBounds(bounds);
                cluster.routeSubscribe(function(id){}, function(cluster, routes) {
                    drawRoutes(routes);
                    drawRouteActions(routes);
                });
            });
        }

        function loadTree() {
            pf.getDefaultCluster(function(cluster) {
                $('#clustertree').treeview({
                    data: [tree(cluster)],
                    showBorder: false,
                    onNodeSelected: function(event, data) {
                        $("#forcerefresh").prop("disabled", false);
                        renderers.forEach(function(v, k, m) { v.setMap(null); });
                        renderers = new Map();
                        markers.forEach(function(v, k, m) { v.setMap(null); });
                        markers = new Map();
                        updateMap(currentSubclusterId());
                    }
                });
            });
        }

        function createSubcluster() {
            var name = $("#subclusterinput").val();
            var parent = currentSubclusterId();
            var path = parent + "/" + name;
            pf.createCluster(path, function(cluster) {
                loadTree();
            });
        }

        loadTree();
        $("#createsubcluster").click(createSubcluster);
        $("#forcerefresh").click(function() { pf.recalculate(currentSubclusterId()) });
    }

    google.maps.event.addDomListener(window, 'load', initializeMap);
});

window.changeProvider = function() {
    document.getElementById("custom_auth_url").disabled = !document.getElementById("custom_auth_url_radio").checked;
}
