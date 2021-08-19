am4core.useTheme(am4themes_animated);
am4core.useTheme(am4themes_material);
var chart = am4core.create("chartdiv", am4plugins_timeline.SerpentineChart);
chart.language.locale["_date_day"] = "dd/MM";

chart.curveContainer.padding(100, 50, 50, 0);
chart.levelCount = 3;
chart.yAxisRadius = am4core.percent(20);
chart.yAxisInnerRadius = am4core.percent(3);
chart.maskBullets = false;

var colorSet = new am4core.ColorSet();
var baseUrl = "/room/"
chart.dateFormatter.inputDateFormat = "m s";


chart.data = [
    {
        "text": "Java Basic",
        "content": "Lập trình căn bản với Java",
        "link": [
            {
                "name": "Code Academy",
                "id": "https://www.codecademy.com/learn/learn-java"
            }
        ]
    },
    {
        "text": "Java OOP",
        "content": "Lập trình hướng đối tượng với Java",
        "link": [
            {
                "name": "W3 Schools",
                "id": "https://www.w3schools.com/java/java_oop.asp"
            }
        ]
    },
    {
        "text": "Java Essential",
        "content": "Sử dụng các lớp Java Essential",
        "link": [
            {
                "name": "Oracle Docs",
                "id": "https://docs.oracle.com/javase/tutorial/"
            }
        ]
    },
    {
        "text": "intellij IDEA ",
        "icon": "/images/tool.svg",
        "category": " ",
        "link": [
            {
                "name": "Jet Brains",
                "id": "https://www.jetbrains.com/help/idea/getting-started.html"
            }
        ]
    },
    {
        "text": "Java GUI",
        "icon": "/images/app.svg",
        "link": [
            {
                "name": "Javatpoint",
                "id": "https://www.javatpoint.com/java-swing"
            }
        ]
    },
    {
        "text": "Socket",
        "icon": "/images/network.svg",
        "link": [
            {
                "name": "Infoworld",
                "id": "https://www.infoworld.com/article/2853780/socket-programming-for-scalable-systems.html"
            }
        ]
    },
    {
        "text": "HTML",
        "icon": "/images/code.svg",
        "category": " ",
        "link": [
            {
                "name": "W3 Schools",
                "id": "https://www.w3schools.com/html/default.asp"
            }
        ]
    },
    {
        "text": "Servlet",
        "link": [
            {
                "name": "Javatpoint",
                "id": "https://www.javatpoint.com/servlet-tutorial"
            }
        ],
        "lab": [
            {
                "name": "Servlet Helloworld",
                "id": "c2A7FR"
            },
            {
                "name": "Form - Collaboration - Config - Context",
                "id": "AVhBz0"
            },
            {
                "name": "Cookie - Session",
                "id": "VMUxUn"
            },
            {
                "name": "Event Listener",
                "id": "L3AQvs"
            },
            {
                "name": "Filter",
                "id": "G9OehA"
            },
            {
                "name": "Annotation",
                "id": "o4Ersx"
            },
            {
                "name": "Textfile",
                "id": "ckm9AE"
            },
            {
                "name": "Upload and Download",
                "id": "urTsqo"
            },
            {
                "name": "Login",
                "id": "QvVu4f"
            },
            {
                "name": "Database Access",
                "id": "6vx3G0"
            }
        ]
    },
    {
        "text": "CSS",
        "icon": "/images/web.svg",
        "category": " ",
        "link": [
            {
                "name": "W3 Schools",
                "id": "https://www.w3schools.com/css/default.asp"
            }
        ]
    },
    {
        "text": "RDBMS",
        "icon": "/images/database.svg",
        "category": " ",
        "link": [
            {
                "name": "Mysql Tutorial",
                "id": "https://www.mysqltutorial.org/"
            }
        ]
    },
    {
        "text": "JSP",
        "link": [
            {
                "name": "Javatpoint",
                "id": "https://www.javatpoint.com/jsp-tutorial"
            }
        ],
        "lab": [
            {
                "name": "JSP Tag",
                "id": "nIwBuu"
            },
            {
                "name": "JSP Implicit Objects",
                "id": "31YcoD"
            },
            {
                "name": "JSP directives",
                "id": "HsnuTs"
            },
            {
                "name": "JSP Action Tags",
                "id": "pJ3UJk"
            },
            {
                "name": "Expression Language",
                "id": "bAf1Ke"
            },
            {
                "name": "JSP Standard Tag Library (JSTL)",
                "id": "P7c9uw"
            },
            {
                "name": "MVC Model 1",
                "id": "CzKHi1"
            },
            {
                "name": "MVC Model 2",
                "id": "uDQPmC"
            }
        ]
    },
    {
        "text": "JPA",
        "icon": "/images/block.svg",
        "link": [
            {
                "name": "Javatpoint",
                "id": "https://www.javatpoint.com/jpa-tutorial"
            }
        ],
        "lab": [
            {
                "name": "JPA Basic",
                "id": "1CoyBL"
            },
            {
                "name": "Query",
                "id": "aCM6Z4"
            },
            {
                "name": "Embed",
                "id": "aGqcQm"
            },
            {
                "name": "Relationship",
                "id": "kr0o6E"
            },
            {
                "name": "Cascading",
                "id": "1SxPtt"
            }
        ]
    }
    ,
    {
        "text": "Maven",
        "icon": "/images/tool.svg",
        "category": " ",
        "link": [
            {
                "name": "Apache Maven",
                "id": "https://maven.apache.org/guides/index.html"
            }
        ]
    }
    ,
    {
        "text": "MVC",
        "icon": "/images/layer.svg",
        "category": " ",
        "link": [
            {
                "name": "MVC in JSP",
                "id": "https://www.javatpoint.com/MVC-in-jsp"
            }
        ],
        "lab": [
            {
                "name": "MVC Model 1",
                "id": "CzKHi1"
            },
            {
                "name": "MVC Model 2",
                "id": "uDQPmC"
            }
        ]
    }
    ,
    {
        "text": "noSQL",
        "icon": "/images/bracket.svg",
        "category": " ",
        "link": [
            {
                "name": "MongoDB University",
                "id": "https://university.mongodb.com/"
            },
            {
                "name": "MongoDB Java Drive",
                "id": "https://mongodb.github.io/mongo-java-driver/4.3/"
            }
        ],
        "lab": [
            {
                "name": "MongoDB",
                "id": "UZJr83"
            },
            {
                "name": "MongoDB Java Basic",
                "id": "BncrHF"
            },
            {
                "name": "Mongo Atlas",
                "id": "bi9CnV"
            },
            {
                "name": "MongoDB Aggregation",
                "id": "uoqmex"
            },
            {
                "name": "MongoDB Indexes",
                "id": "BWghAT"
            },
            {
                "name": "MongoDB Java Advanced",
                "id": "OLrfqE"
            }
        ]
    }
    ,
    {
        "category": "  ",
        "text": "Project 1",
        "icon": "/images/idea.svg",

        "lab": [
            {
                "name": "Mflix 1",
                "id": "xncTjt"
            },
            {
                "name": "Mflix 2",
                "id": "RFFH4T"
            },
            {
                "name": "Mflix 3",
                "id": "tvj14u"
            },
            {
                "name": "Mflix 4",
                "id": "6fNT47"
            },
            {
                "name": "Mflix 5",
                "id": "02qHiD"
            }
            ,
            {
                "name": "Mflix 6",
                "id": "B1H83u"
            }
        ]
    }
    ,
    {
        "text": "Bootstrap",
        "icon": "/images/b.svg",
        "category": " ",
        "link": [
            {
                "name": "Getbootstrap",
                "id": "https://getbootstrap.com/docs/5.0/getting-started/introduction/"
            }
        ]
    }
    ,
    {
        "text": "Thymeleaf",
        "icon": "/images/leaf.svg",
        "link": [
            {
                "name": "Thymeleaf",
                "id": "https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html"
            }
        ],
        "lab": [
            {
                "name": "Thymeleaf",
                "id": "UgScHX"
            }
        ]
    }
    ,
    {
        "text": "Firebase",
        "icon": "/images/firebase.svg",
        "category": " ",
        "link": [
            {
                "name": "Authentication",
                "id": "https://firebase.google.com/docs/auth"
            }
        ],
        "lab": [
            {
                "name": "Hello Firebase",
                "id": "tNG1z3"
            }
        ],
    }
    ,
    {
        "text": "Jersey",
        "icon": "/images/jersey.svg",
        "link": [
            {
                "name": "Jersey User Guide",
                "id": "https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/index.html"
            }
        ],
        "lab": [
            {
                "name": "Jersey Client",
                "id": "WobKq2"
            },
             {
                "name": "Jersey Server",
                "id": "JENBWx"
            }
        ]
    },
    {
        "text": "jQuery",
        "icon": "/images/jquery.svg",
        "category": " ",
        "link": [
            {
                "name": "jQuery Learning Center",
                "id": "https://learn.jquery.com/"
            }
        ],
        "lab": [
            {
                "name": "jQuery",
                "id": "TxnyPQ"
            }
        ]
    }
];

for (let i = 0; i < chart.data.length; i++) {
    if (chart.data[i].category == null)
        chart.data[i].category = "   ";
    if (chart.data[i].start == null)
        chart.data[i].start = (i + 1) + ""
}

for (let i = 0; i < chart.data.length; i++) {
    if (chart.data[i].end == null) {
        var found = false;
        var end = false;
        var j;
        for (j = i + 1; j < chart.data.length; j++)
            if (chart.data[j].category === chart.data[i].category) {
                chart.data[i].end = chart.data[j].start;
                found = true;
                break;
            }
        if (j == chart.data.length) {
            chart.data[i].end = (chart.data.length) + "";
        }

    }
    if (chart.data[i].color == null){
        chart.data[i].color = colorSet.getIndex(i)
       chart.data[i].color = colorSet.getIndex(29 - Math.round(i * 10 / chart.data.length))
    }
    chart.data[i].textDisabled = false;

    if (i == chart.data.length - 1)
        chart.data[i].end = (chart.data[i].start) + " 1";
    if (chart.data[i].icon == null) {
        chart.data[i].icon = "/images/java.svg"
    }
}

chart.fontSize = 14;
chart.tooltipContainer.fontSize = 10;

var categoryAxis = chart.yAxes.push(new am4charts.CategoryAxis());
categoryAxis.dataFields.category = "category";
categoryAxis.renderer.grid.template.disabled = true;
categoryAxis.renderer.labels.template.paddingRight = 25;
categoryAxis.renderer.minGridDistance = 10;

var dateAxis = chart.xAxes.push(new am4charts.DateAxis());
dateAxis.renderer.minGridDistance = 1;
dateAxis.baseInterval = {count: 1, timeUnit: "minute"};
dateAxis.dateFormats.setKey('minute', 'mm');
dateAxis.renderer.tooltipLocation = 0;
dateAxis.renderer.line.strokeDasharray = "1,4";
dateAxis.renderer.line.strokeOpacity = 0.4;
dateAxis.tooltip.background.fillOpacity = 0.2;
dateAxis.tooltip.background.cornerRadius = 5;
dateAxis.tooltip.label.fill = new am4core.InterfaceColorSet().getFor("alternativeBackground");
dateAxis.tooltip.label.paddingTop = 7;
dateAxis.endLocation = 0;
dateAxis.startLocation = 0;

var labelTemplate = dateAxis.renderer.labels.template;
labelTemplate.verticalCenter = "middle";
labelTemplate.fillOpacity = 0.6;
labelTemplate.background.fill = am4core.color("#eceff1");
labelTemplate.background.fillOpacity = 1;
labelTemplate.padding(5, 5, 5, 5);

var series = chart.series.push(new am4plugins_timeline.CurveColumnSeries());
series.columns.template.height = am4core.percent(12);

series.dataFields.openDateX = "start";
series.dataFields.dateX = "end";
series.dataFields.categoryY = "category";
series.baseAxis = categoryAxis;
series.columns.template.propertyFields.fill = "color"
series.columns.template.propertyFields.stroke = "color"
series.columns.template.strokeOpacity = 0;
series.columns.template.fillOpacity = 0.7;

var imageBullet1 = series.bullets.push(new am4plugins_bullets.PinBullet());
imageBullet1.locationX = 1;
imageBullet1.propertyFields.stroke = "color"
imageBullet1.background.propertyFields.fill = "color"

imageBullet1.image = new am4core.Image();
imageBullet1.image.propertyFields.href = "icon";
imageBullet1.image.scale = 0.5;
imageBullet1.circle.radius = am4core.percent(100);
imageBullet1.dy = -5;

var textBullet = series.bullets.push(new am4charts.LabelBullet());
textBullet.label.propertyFields.text = "text";
textBullet.disabled = true;
textBullet.propertyFields.disabled = "textDisabled";
textBullet.label.strokeOpacity = 0;
textBullet.locationX = 1;
textBullet.dy = -95;
textBullet.label.textAlign = "middle";

var label = chart.createChild(am4core.Label);
label.text = "JAVA ROADMAP"
label.isMeasured = false;
label.y = am4core.percent(33);
label.x = am4core.percent(48);
label.horizontalCenter = "middle";
label.fontSize = 22;

function myFunction(ev) {
    var data = chart.data[ev.target.dataItem.index];
    $('#modal-title').html(data.text);
    var content = "";
    if (data.content != null)
        content = "<h6>" + data.content + "</h6>"

    content = content + "<table class='table table-borderless' id='table-rooms'>"

    if (data.link != null) {
        content = content + "<tr><td><b>Links:</b></td></tr>";
        for (let i = 0; i < data.link.length; i++) {
            let row = "<tr id='" + data.link[i].id + "'><td class='align-middle'>" + (i + 1) + ". " + data.link[i].name + "</td><td class='text-end align-middle'> <a href='" + data.link[i].id + "' class='text-primary' target='_blank'>Link</a></td> <td class='text-end align-middle'><a href='#' class='bi bi-three-dots-vertical link-dark d-none' data-bs-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='xem(\"" + data.link[i].id + "\")'>Xóa</a> </div></td></tr>";
            content = content + row;
        }
    }
    if (data.lab != null) {
        content = content + "<tr><td><b>Labs:</b></td></tr>";
        for (let i = 0; i < data.lab.length; i++) {
            let row = "<tr id='" + data.lab[i].id + "'><td class='align-middle'>" + (i + 1) + ". " + data.lab[i].name + "</td><td class='text-end align-middle'> <a href='" + baseUrl + data.lab[i].id + "' class='text-primary' target='_blank'>Vào phòng</a></td> <td class='text-end align-middle'><a href='#' class='bi bi-three-dots-vertical link-dark d-none' data-bs-toggle='dropdown'></a> <div class='dropdown-menu'><a class='dropdown-item' href='#' onclick='xem(\"" + data.lab[i].id + "\")'>Xóa</a> </div></td></tr>";
            content = content + row;
        }
    }
    content = content + "</table>";

    $('#modal-body').html(content);
    $('#modal-info').modal('toggle'); // Opens the dropdown
}

imageBullet1.events.on("hit", myFunction, this);


