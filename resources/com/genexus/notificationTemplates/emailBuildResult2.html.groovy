<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <style>
        .gray-container {
            z-index:-1;
            background-color: #f4f4f4;
            margin: 10px;
        }
        .jenkins-header {
            padding-top: 25px;
            display: flex;
            width:  1fr;
            height: 150px;
            flex-wrap: nowrap;
            /* background-color: red; */
        }
        .jenkins-header-icon {
            width:  120px;
            display: block;
            /* background-color: green; */
        }
        .jenkins-header-tittle {
            width:  100%;
            padding-top: 15px;
            height: auto;
            display: block;
        }
        .jenkins-header-outputlink {
            width:  200px;
            display: block;
            /* background-color: blue; */
        }

        .jenkins-header-tittleline {
            width:  100%;
            height: 55px;
            display: block;
        }
        #jenkins-logo{
            width:  100px;
            height: auto;
            float: left;
            padding-top: 10px;
            padding-left: 30px;
            display: block;
        }
        #build-result {
            display: block;
            margin: 0px;
            padding-left: 30px;
            font-family: Arial, sans-serif;
            font-size: 25px;
        }
        #jenkins-output{
            width:  270px;
            height: auto;
            float: right;
            padding: 10px;
        }

        .jenkins-properties {
            display: flex;
            width:  1fr;
            flex-wrap: nowrap;
            /* background-color: blue; */
        }

        .jenkins-properties-nav {
            display: block;
            width: 600px;
            /* background-color: brown; */
        }

        #build-properties {
            list-style-type: none; /* Remove bullets */
            padding: 0; /* Remove padding */
            margin-left: 25px; /* Remove margins */
            font-size: 15px;
        }

        .jenkins-properties-main {
            display: block;
            width: 100%;
            border-style: inset;
            /* background-color: gold; */
        }
        
		.jenkins-revisions {
			width:100%;
			text-align: center;
		}
		.revisions-even {
			background-color:#AEB6BF;
		}
		.revisions-odd {
			background-color:#D6DBDF;
		}
		.revision-item {
			font-size: 10pt;
			font-weight: normal;
		}
        .jenkins-header-outputlink a:hover {
            background-color: #0056b3;
        }
        </style>
</head>
<body>
    <div class="gray-container">
        <div class="jenkins-header">
            <div class="jenkins-header-icon">
                <img id="jenkins-logo" src="https://raw.githubusercontent.com/Enz1n/Images/main/images/1200px-Jenkins_logo_with_title.svg%20(1).png">
            </div>
            <div class="jenkins-header-tittle">
                <div class="jenkins-header-tittleline">
                    <img id="jenkins-logo" src="https://raw.githubusercontent.com/Enz1n/Images/main/images/jenkinstitle.png">
                </div>
                <div class="jenkins-header-tittleline">
                    <p style="color: ${buildColor};" id="build-result" >Build #${buildNumber} <strong>${buildResult}</p>
                </div>
            </div>
            <div class="jenkins-header-outputlink">
                <a href="${jenkinsUrl}" style="
                    display: inline-block;
                    font-weight: bold;
                    color: #fff;
                    background-color: #007bff;
                    padding: 10px 20px;
                    text-decoration: none;
                    border-radius: 5px;
                    transition: background-color 0.3s ease;
                    width: 250px;
                ">
                    See Pipeline Output
                </a>
            </div>
        </div>
        <div class="jenkins-properties">
            <div class="jenkins-properties-nav">
                <ul id="build-properties">
                    <li><strong>Date:</strong> ${jenkinsTimestamp}</li>
                    <li><strong>Duration:</strong> ${jenkinsDuration}</li>
                    <li>${cause}</li>
                    <li><strong>GeneXus Version:</strong> ${gxversion}</li>
                    ${extraGralInfo}
                </ul>
            </div>
            <div class="jenkins-properties-main">
				<table class="jenkins-revisions">
				  <tr class="revisions-even">
					<th class="revision-item">
						<p style="margin:0px;padding-left:5px;font-size: 12pt;text-align: left;">Modifications since last build</p>
					</th>
				  </tr>
				</table>
				<table class="jenkins-revisions">
                    ${changeLogSet}
                </table>
            </div>
        </div>
    </div>
</body>
</html>