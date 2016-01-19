$(function() {
    function getTree() {
        return [{
            text: "application id",
            nodes: [
                {
                    text: "USA",
                    nodes: [
                        {
                            text: "NYC",
                        },
                        {
                            text: "SF"
                        }
                    ]
                },
                {
                    text: "UK",
                    nodes: [
                        {
                            text: "London"
                        }
                    ]
                }
            ]
        }]
    }
    console.log("Made it this far");
    $('#clustertree').treeview({
        data: getTree(),
        showBorder: false,
    });
});

function initializeMap() {
    var mapCanvas = document.getElementById("map");
    var mapOptions = {
        center: new google.maps.LatLng(37.7833, -122.4167),
        zoom: 11,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        disableDefaultUI: true,
        zoomControl: true
    };
    var map = new google.maps.Map(mapCanvas, mapOptions);
}

google.maps.event.addDomListener(window, 'load', initializeMap);
