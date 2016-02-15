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

        var renderers = {};
        function drawRoute(route) {
            if (route.actions.length < 2) return;
            var rendererKey = JSON.stringify(route);
            var newRenderers = {};
            if (!(rendererKey in renderers)) {
                var directionsDisplay = new google.maps.DirectionsRenderer({
                    preserveViewport: true,
                    suppressMarkers: true
                });
                newRenderers[rendererKey] = directionsDisplay;
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
                directionsService.route(request, function (result, status) {
                    if (status == google.maps.DirectionsStatus.OK) {
                        directionsDisplay.setDirections(result);
                    }
                });
            } else {
                newRenderers[rendererKey] = renderers[rendererKey];
            }
            renderers.forEach(function(k, v) { v.setMap(null); });
            renderers = newRenderers;
        }

        var markers = {};
        var displaycluster = undefined;

        function drawRouteActions(routes) {
            var newMarkers = {};
            console.log("Drawing route actions");
            console.log(routes);
            var vehicles = routes.map(function(x) { return x.vehicle; });
            var actions = [].concat.apply([], routes.map(function(x) { return x.actions; }));
            console.log(vehicles);
            console.log(actions);
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
                if (markerKey in markers) {
                    newMarkers[markerKey] = markers[markerKey];
                } else {
                    newMarkers[markerKey] = new google.maps.Marker({
                        position: { lat: action.latitude, lng: action.longitude },
                        map: map,
                        label: label
                    });
                }
            });
            markers.forEach(function(k, v) {
                if (!(k in newMarkers)) v.setMap(null);
            });
            markers = newMarkers
            if (maxLng < minLng) return;
            var bounds = {
                "east": maxLng,
                "west": minLng,
                "north": maxLat,
                "south": minLat
            };
            console.log("Bounds:");
            console.log(bounds);
            map.fitBounds(bounds);
            map.panToBounds(bounds);
        }

        function updateMap(path) {
            $("#maplabel").text("Cluster id: " + path);
            if (displaycluster) {
                displaycluster.routeUnsubscribe();
            }
            pf.getCluster(path, function(cluster) {
                markers.forEach(function(k, v) { v.setMap(null); });
                markers = {};
                displaycluster = cluster;
                var maxLat = -90
                    ,minLat = 90
                    ,maxLng = -180
                    ,minLng = 180;
                cluster.commodities.forEach(function(commodity) {
                    maxLat = Math.max(maxLat, commodity.startLat, commodity.endLat);
                    minLat = Math.min(minLat, commodity.startLat, commodity.endLat);
                    maxLng = Math.max(maxLng, commodity.startLng, commodity.endLng);
                    minLng = Math.min(minLng, commodity.startLng, commodity.endLng);
                    markers[JSON.stringify(commodity)] = new google.maps.Marker({
                        position: { lat: commodity.startLat, lng: commodity.startLng },
                        map: map,
                        label: "P"
                    });
                    markers[JSON.stringify(commodity)] = new google.maps.Marker({
                        position: { lat: commodity.endLat, lng: commodity.endLng },
                        map: map,
                        label: "D"
                    });
                });
                cluster.transports.forEach(function(transport) {
                    maxLat = Math.max(maxLat, transport.lat);
                    minLat = Math.min(minLat, transport.lat);
                    maxLng = Math.max(maxLng, transport.lng);
                    minLng = Math.min(minLng, transport.lng);
                    markers[JSON.stringify(transport)] = new google.maps.Marker({
                        position: { lat: transport.lat, lng: transport.lng },
                        map: map,
                        label: "Transport"
                    });
                });
                if (maxLng < minLng) return;
                var bounds = {
                    "east": maxLng,
                    "west": minLng,
                    "north": maxLat,
                    "south": minLat
                };
                map.fitBounds(bounds);
                map.panToBounds(bounds);
                cluster.routeSubscribe(function(id){}, function(cluster, routes) {
                    routes.forEach(drawRoute);
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
                        renderers.forEach(function(k, v) { v.setMap(null); });
                        renderers = {};
                        markers.forEach(function(k, v) { v.setMap(null); });
                        markers = {};
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
    }

    google.maps.event.addDomListener(window, 'load', initializeMap);
});
