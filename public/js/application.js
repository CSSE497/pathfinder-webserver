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
            return node.text;
        }
    }

    function currentSubclusterId() {
        var selected = $("#clustertree").treeview('getSelected')[0];
        return subclusterId(selected.nodeId);
    }

    function createSubcluster() {
        var name = $("#subclusterinput").val();
        var parent = currentSubclusterId();
        var path = parent + "/" + name;
        pf.createCluster(path, function(cluster) {
            loadTree();
        });
    }

    function updateMap(path) {
        $("#maplabel").text("Cluster: " + path);
        pf.getCluster(path, function(cluster) {
            console.log("Attempting to create map for");
            console.log(cluster);


        });
    }

    function loadTree() {
        pf.getDefaultCluster(function(cluster) {
            $('#clustertree').treeview({
                data: [tree(cluster)],
                showBorder: false,
                onNodeSelected: function(event, data) {
                    updateMap(currentSubclusterId());
                }
            });
        });
    }

    loadTree();
    $("#createsubcluster").click(createSubcluster);
    $("#createsubcluster").prop("disabled", true);
    $("#subclusterinput").keyup(function() {
        $("#createsubcluster").prop("disabled", this.value == "" ? true : false);
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
