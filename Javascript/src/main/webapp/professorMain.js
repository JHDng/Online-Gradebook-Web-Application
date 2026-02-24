/**
 * 
 */
{
	var pageManager = new PageManager();
	
	window.addEventListener("load", () => {
	    if (sessionStorage.getItem("user") == null) {
	     	window.location.href = "login.html";
	    } else {
	      	pageManager.start();
	    } // display initial content
 	}, false);
	
	  
  	function PageManager() {
		var courseFetcher = new CourseFetcher();
		var dateFetcher = new DateFetcher();
		var buttonsHomePage = new ButtonsHomePage();
		
		var registeredStudentsFetcher = new RegisteredStudentsFetcher();
		var buttonsRSP = new ButtonsRSP();

		var gradableStudentsFetcher = new GradableStudentsFetcher();
		var buttonsModal = new ButtonsModal();
		
		var recordsFetcher = new RecordsFetcher();
		var buttonsER = new ButtonsER();

		var recordFetcher = new RecordFetcher();
		var buttonsRP = new ButtonsRP();
		
		var studentInfoFetcher = new StudentInfoFetcher();
		var buttonsGF = new ButtonsGF();
		
		var errorText = document.getElementById("id_errorText");
		
		var self = this;
		
  	    this.start = function() {
			var username = JSON.parse(sessionStorage.getItem('user')).username;
 			var personalMessage = new PersonalMessage(username, document.getElementById("id_username"));
			personalMessage.show();
			
			document.getElementById("id_errorText").style.display = "none";
			document.getElementById("id_errorText").style.color = "red";
			
			registeredStudentsFetcher.setupReorder();
			
			document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        	window.sessionStorage.removeItem('username');
	  		})
			
			self.goToHomepage();
		}
		
		this.refresh = function() {
			courseFetcher.reset();
			dateFetcher.reset();
			buttonsHomePage.reset();
			
			registeredStudentsFetcher.reset();
			buttonsRSP.reset();

			gradableStudentsFetcher.reset();
			buttonsModal.reset();
			
			recordsFetcher.reset();
			buttonsRP.reset();

			recordFetcher.reset();
			buttonsER.reset();

			studentInfoFetcher.reset();
			buttonsGF.reset();
			
			errorText.style.display = "none";
		};
		
		this.fetchDates = function(_courseID) {
			dateFetcher.show(_courseID);
		}
		
		this.goToRegisteredStudents = function(_examDateID) {
			self.refresh();
			registeredStudentsFetcher.show(_examDateID);
			buttonsRSP.show(_examDateID);
		}
		
		this.goToGradeForm = function(_examDateID, _studentID) {
			self.refresh();
			studentInfoFetcher.show(_examDateID, _studentID);
			buttonsGF.show();
		}
		
		this.goToHomepage = function() {
			self.refresh();
			courseFetcher.show();
			buttonsHomePage.show();
		}
		
		this.goToExamRecord = function (_recordID) {
			self.refresh();
            recordFetcher.show(_recordID);
			buttonsER.show();
		}
		
		this.goToRecords = function() {
			self.refresh();
            recordsFetcher.show();
			buttonsRP.show();
		}
		
		this.goToModal = function(_examDateID) {
			self.refresh();
			gradableStudentsFetcher.show(_examDateID);
			buttonsModal.show(_examDateID);
		}
		
		this.showErrorText = function(_text) {
			errorText.style.display = "block";
			errorText.textContent = _text;
		}
	}
		  
	function PersonalMessage(_username, _messagecontainer) {
		this.username = _username;
		this.show = function() {
			_messagecontainer.textContent = this.username;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//HOMEPAGE///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	function CourseFetcher() {
		var self = this;
		
		this.show = function() {
			sendReq("GET", 'GetCoursesProfessor', null,
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						if(x.status == 200){
							self.update(message);
						} else if(x.status == 403) {
							window.location.href = x.getResponseHeader("Location");
				        	window.sessionStorage.removeItem('user');
						} else {
							pageManager.showErrorText(message);
						}
					}
				}
			);
		}
		
		this.update = function(_json) {
			var courseList = JSON.parse(_json); 
			var listElement = document.getElementById("id_coursesList");
			
			listElement.innerHTML = "";

			courseList.forEach(course => {
				var li = document.createElement("li");
				var a = document.createElement("a");

				a.href = "#"; // Prevents page reload; you can leave it if not needed
				a.textContent = course.name;

				a.addEventListener("click", e => {
					e.preventDefault(); // Prevent default behavior
					pageManager.fetchDates(course.courseID);
				});

				li.appendChild(a);
				listElement.appendChild(li);
			});
			
			listElement.style.display = "block";
			document.getElementById("id_courses").style.display = "block";
			document.getElementById("id_welcomeHeader").style.display = "block";
		}
		
		this.reset = function() {
			document.getElementById("id_welcomeHeader").style.display = "none";
			document.getElementById("id_errorText").style.display = "none";
			document.getElementById("id_coursesList").style.display = "none";
			document.getElementById("id_courses").style.display = "none";
		}
	}
	
	function DateFetcher() {
		var self = this;
		
		this.show = function(_courseID) {
			sendReq("GET", 'GetDatesProfessor?courseID=' + encodeURIComponent(_courseID), null,
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						self.reset();
						if(x.status == 200) {
							self.update(message);
						} else if(x.status == 403) {
							window.location.href = x.getResponseHeader("Location");
				        	window.sessionStorage.removeItem('user');
						} else {
							pageManager.showErrorText(message);
						}
					}
				}
			);
		}
		
		this.update = function(_json) {
			var dateList = JSON.parse(_json); 
			var listElement = document.getElementById("id_datesList");
			listElement.innerHTML = "";

			dateList.forEach(d => {
				var li = document.createElement("li");
				var a = document.createElement("a");

				a.href = "#"; // Prevents page reload; you can leave it if not needed
				a.textContent = d.date;
				a.setAttribute("examDateID", d.examDateID);
				
				a.removeEventListener("click", handleDateClick);
				a.addEventListener("click", handleDateClick);

				li.appendChild(a);
				listElement.appendChild(li);
			});
			
			listElement.style.display = "block";
		}
		
		function handleDateClick(event) {
			event.preventDefault();
			pageManager.goToRegisteredStudents(event.target.getAttribute("examDateID"));
		}
		
		this.reset = function() {
			document.getElementById("id_errorText").style.display = "none";
			document.getElementById("id_datesList").style.display = "none";
		}
	}
	
	function ButtonsHomePage() {
		
		this.show = function() {
			var recordsButton = document.getElementById("id_recordsHome");
			recordsButton.style.display = "block";
			
			recordsButton.removeEventListener("click", handleGoToRecordsFunction);
			recordsButton.addEventListener("click", handleGoToRecordsFunction);
		}
		
		this.reset = function() {
			document.getElementById("id_recordsHome").style.display = "none";
		}
		
		function handleGoToRecordsFunction(event) {
			event.preventDefault();
			pageManager.goToRecords();
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//REGISTEREDSTUDENTS/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	function RegisteredStudentsFetcher() {
		var self = this;
		var examDateID = null;
		this.show = function(_examDateID) {
			
			if(_examDateID)
				examDateID = _examDateID
			
			var url = 'GetRegisteredStudents?' +
			        '&examDateID=' + encodeURIComponent(examDateID);
			
			sendReq("GET", url, null,
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						if(x.status == 200) {
							self.update(message);
						} else if(x.status == 403) {
							window.location.href = x.getResponseHeader("Location");
				        	window.sessionStorage.removeItem('user');
						} else {
							pageManager.showErrorText(message);
						}
					}
				}
			);
		}
		
		this.update = function(_json) {
			var registrations = JSON.parse(_json);
			var tableBody = document.querySelector('#id_studentsTable tbody');

		    tableBody.innerHTML = ""; // Clear previous rows

		    registrations.forEach(reg => {
		        var student = reg.student;
		        var gradeStatus = reg.gradeStatus;

		        var row = document.createElement("tr");

		        var cell = [
		            student.studentID,
		            student.surname,
		            student.name,
		            student.email,
		            student.major,
		            gradeStatus.grade,
		            gradeStatus.status
		        ];

		        cell.forEach((value, index) => {
		            var td = document.createElement("td");
					
					if(index == 5) {
						if (value === -1) td.textContent = "Absent";
					    else if (value === -2) td.textContent = "Failed";
					    else if (value === -3) td.textContent = "Failed (Deferred)";
					    else if (value === 31) td.textContent = "30 cum laude";
					    else if (value >= 0 && value <= 30) td.textContent = value;
						td.setAttribute("gradeValue", value);
					} else {
						td.textContent = value;
					}
					
					row.appendChild(td);
		        });
				
				if(gradeStatus.status !== "published" && gradeStatus.status !== "recorded" && gradeStatus.status !== "refused") {
					var modifyButton = document.createElement("button")
					modifyButton.textContent = "MODIFY";
					modifyButton.setAttribute("type", "button");
					modifyButton.addEventListener("click", e => {
							e.preventDefault();
							pageManager.goToGradeForm(examDateID, student.studentID);
						}
					)
					row.appendChild(modifyButton);
				}

		        tableBody.appendChild(row);
		    });

			document.getElementById("id_studentsTable").style.display = "block";
		}
		
		this.setupReorder = function() {
			var currentSortField = null;
			var currentSortAscending = true;

			document.querySelectorAll("th a.sortable").forEach(a => {
				a.addEventListener("click", e => {
			        e.preventDefault();

			        var field = a.textContent.trim().toLowerCase();

			        if (currentSortField === field) {
			            currentSortAscending = !currentSortAscending;
			        } else {
			            currentSortField = field;
			            currentSortAscending = true;
			        }
					
					var th = a.closest("th");
					var index = Array.from(th.parentNode.children).indexOf(th);
			        sortTable(index, currentSortAscending);
			    });
			});
			
			function sortTable(columnIndex, ascending) {
			    var tbody = document.querySelector("#id_studentsTable tbody");
			    var rows = Array.from(tbody.querySelectorAll("tr"));
				
				if(columnIndex == 0){					
				    rows.sort((a, b) => {
				        var aText = a.children[columnIndex].textContent.trim();
				        var bText = b.children[columnIndex].textContent.trim();
	
				        var aNum = parseInt(aText);
				        var bNum = parseInt(bText);
	
				        var res = aNum - bNum;
	
				        return ascending ? res : -res;
				    });
				} else if(columnIndex == 5) {
					rows.sort((a, b) => {
				        var aText = a.children[columnIndex].getAttribute("gradeValue").trim();
				        var bText = b.children[columnIndex].getAttribute("gradeValue").trim();
	
				        var aNum = parseInt(aText);
				        var bNum = parseInt(bText);
	
				        var res = aNum - bNum;
	
				        return ascending ? res : -res;
				    });
				} else {
					rows.sort((a, b) => {
				        var aText = a.children[columnIndex].textContent.trim();
				        var bText = b.children[columnIndex].textContent.trim();
	
				        var res = aText.localeCompare(bText, 'it', { sensitivity: 'base' });
	
				        return ascending ? res : -res;
			    	});
				}

			    rows.forEach(row => tbody.appendChild(row));
			}
		}
		
		this.reset = function() {
			document.getElementById("id_studentsTable").style.display = "none";
			document.getElementById("id_errorText").style.display = "none";
		}
	}
	
	function ButtonsRSP(){
		var examDateID = null;
		this.show = function(_examDateID) {
			
			if(_examDateID)
				examDateID = _examDateID;
			
			document.getElementById("id_buttonsRSP").style.display = "block";
			
			var backButton = document.getElementById("id_backButton");
			backButton.removeEventListener("click", handleBackToHomeFunction);
			backButton.addEventListener("click", handleBackToHomeFunction);
			
			var publishButton = document.getElementById("id_publishButton");
			publishButton.removeEventListener("click", handlePublishFunction);
			publishButton.addEventListener("click", handlePublishFunction);
			
			var recordButton = document.getElementById("id_recordButton");
			recordButton.removeEventListener("click", handleRecordFunction);
			recordButton.addEventListener("click", handleRecordFunction);
			
			var multModButton = document.getElementById("id_multipleModificationButton");
			multModButton.removeEventListener("click", handleMultipleModificationsFunction);
			multModButton.addEventListener("click", handleMultipleModificationsFunction);
		}
		
		this.reset = function() {
			document.getElementById("id_buttonsRSP").style.display = "none";
		}
		
		function handleBackToHomeFunction(event) {
			event.preventDefault();
			pageManager.goToHomepage();
		}
		
		function handleRecordFunction(event) {
			event.preventDefault();
		    var url = 'RecordGrades?' + '&examDateID=' + encodeURIComponent(examDateID);

		    sendReq("POST", url, null, function(x) {
		        if (x.readyState === XMLHttpRequest.DONE) {
		            var message = x.responseText;
		            if (x.status === 200) {
		                pageManager.goToExamRecord(message);
		            } else if (x.status === 403) {
		                window.location.href = x.getResponseHeader("Location");
		                window.sessionStorage.removeItem('user');
		            } else {
		               pageManager.showErrorText(message);
		            }
		        }
		    });
			
		}
		
		function handleMultipleModificationsFunction(event) {
			event.preventDefault();
			pageManager.goToModal(examDateID);
		}
		
		function handlePublishFunction(event) {
			event.preventDefault();
		    var url = 'PublishGrades?' + '&examDateID=' + encodeURIComponent(examDateID);
			
		    sendReq("POST", url, null, function(x) {
		        if (x.readyState === XMLHttpRequest.DONE) {
		            var message = x.responseText;
		            if (x.status === 200) {
		                pageManager.goToRegisteredStudents(examDateID);
		            } else if (x.status === 403) {
		                window.location.href = x.getResponseHeader("Location");
		                window.sessionStorage.removeItem('user');
		            } else {
						pageManager.showErrorText(message);
		            }
		        }
		    });
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//MODAL PAGE/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	function GradableStudentsFetcher() {
		var self = this;
		var examDateID = null;
		this.show = function(_examDateID){
			if(_examDateID)
				examDateID = _examDateID;
		    var url = 'GetGradableStudents?' + '&examDateID=' + encodeURIComponent(_examDateID);
		    sendReq("GET", url, null, function(x) {
		        if (x.readyState === XMLHttpRequest.DONE) {
		            var message = x.responseText;
		            if (x.status === 200) {
		                self.update(message);
		            } else if (x.status === 403) {
		                window.location.href = x.getResponseHeader("Location");
		                window.sessionStorage.removeItem('user');
		            } else {
		               pageManager.showErrorText(message);
		            }
		        }
		    });
		}
		
		this.update = function(_json) {
			var registrations = JSON.parse(_json);
			var tableBody = document.querySelector('#id_studentsTableModal tbody');
			
			tableBody.innerHTML = ""; // Clear previous rows

			registrations.forEach(reg => {
				var student = reg.student;
				var row = document.createElement("tr");

				var cellValues = [
					student.studentID,
					student.surname,
					student.name,
					student.email,
					student.major
				];

				cellValues.forEach(value => {
					var td = document.createElement("td");
					td.textContent = value;
					row.appendChild(td);
				});

				// Create selection field
				var selectTd = document.createElement("td");
				
				var select = document.createElement("select");
				select.setAttribute("name", student.studentID);

				var options = [
					{ value: "-4", label: "not inserted" },
					{ value: "-1", label: "Absent" },
					{ value: "-2", label: "Failed" },
					{ value: "-3", label: "Failed(Deferred)" },
					{ value: "18", label: 18 },
					{ value: "19", label: 19 },
					{ value: "20", label: 20 },
					{ value: "21", label: 21 },
					{ value: "22", label: 22 },
					{ value: "23", label: 23 },
					{ value: "24", label: 24 },
					{ value: "25", label: 25 },
					{ value: "26", label: 26 },
					{ value: "27", label: 27 },
					{ value: "28", label: 28 },
					{ value: "29", label: 29 },
					{ value: "30", label: 30},
					{ value: "31", label: "30 cum laude" }
				];

				options.forEach(opt => {
					var option = document.createElement("option");
					option.value = opt.value;
					option.textContent = opt.label;
					select.appendChild(option);
				});

				selectTd.appendChild(select);
				row.appendChild(selectTd);

				tableBody.appendChild(row);
			});
			
			console.log(tableBody);
			
			document.getElementById("id_modalForm").style.display = "block";
			
			document.getElementById("id_sendMultipleGrades").addEventListener("click", e => {
				e.preventDefault();
				var tableBody = document.querySelector('#id_studentsTableModal tbody');
				
				var form = e.target.closest("form");
				var inputExamDateID = document.createElement("input");
				inputExamDateID.setAttribute("name", "examDateID");
				inputExamDateID.setAttribute("value", examDateID);
				inputExamDateID.style.display = "none";
				form.append(inputExamDateID);
				
				sendReq("POST", "ModifyMultipleGrades", form, function(x) {
			        if (x.readyState === XMLHttpRequest.DONE) {
			            var message = x.responseText;
			            if (x.status === 200) {
			                pageManager.goToRegisteredStudents();
			            } else if (x.status === 403) {
			                window.location.href = x.getResponseHeader("Location");
			                window.sessionStorage.removeItem('user');
			            } else {
			               pageManager.showErrorText(message);
			            }
			        }
			    })
			});
		}

		
		this.reset = function() {
			document.getElementById("id_modalForm").style.display = "none";
		}
	}
	
	function ButtonsModal() {
		var examDateID = null;
		
		this.show = function(_examDateID) {
			if(_examDateID)
				examDateID = _examDateID;
			var backButton = document.getElementById("id_backToRegStudButton");
			backButton.style.display = "block";
			backButton.removeEventListener("click", handleBackToRegStudFunction);
			backButton.addEventListener("click", handleBackToRegStudFunction);
		}
		
		this.reset = function() {
			document.getElementById("id_backToRegStudButton").style.display = "none";
		}
		
		function handleBackToRegStudFunction(event) {
			event.preventDefault();
			pageManager.goToRegisteredStudents(examDateID);
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//RECORDS////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	function RecordsFetcher() {
		
		var self = this;
		
		this.show = function(){
			sendReq("GET", 'GetRecords', null,
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						if(x.status == 200) {
							self.update(message);
						} else if(x.status == 403) {
							window.location.href = x.getResponseHeader("Location");
				        	window.sessionStorage.removeItem('user');
						} else {
							pageManager.showErrorText(message)
						}
					}
				}
			);
		}
		
		this.update = function(_json) {
			document.getElementById("id_recordsTable").style.display = "block";
			var tbody = document.querySelector("#id_recordsTable tbody");
			var records = JSON.parse(_json);
			
			tbody.innerHTML = "";
			
			records.sort((a, b) => {
			  const nameA = a.course.name.toLowerCase();
			  const nameB = b.course.name.toLowerCase();

			  if (nameA < nameB) return -1;
			  if (nameA > nameB) return 1;

			  return new Date(a.examDate.date) - new Date(b.examDate.date);
			});
			
			records.forEach( rec => {
				var row = document.createElement("tr");
				
				var cell =	[
					rec.course.name,
					rec.examDate.date,
					rec.recordID,
					rec.creationDate,
					rec.creationTime
				]
				
				cell.forEach(value => {
					var td = document.createElement("td");
					td.textContent = value;
					row.appendChild(td);
				})
				
				var navigateButton = document.createElement("button");
				navigateButton.setAttribute("type", "button");
				navigateButton.textContent = "NAVIGATE";
				navigateButton.addEventListener("click", e => {
					e.preventDefault();
					pageManager.goToExamRecord(rec.recordID);
				})
				row.appendChild(navigateButton);
				
				tbody.appendChild(row);
			})
		}
		
		this.reset = function() {
			document.getElementById("id_recordsTable").style.display = "none";
		}
	}
		
	function ButtonsRP() {
		this.show = function() {
			var backToHomeButton = document.getElementById("id_buttonRP");
			
			backToHomeButton.style.display = "block";
			backToHomeButton.removeEventListener("click", handleBackToHomeFunction);
			backToHomeButton.addEventListener("click", handleBackToHomeFunction);
		}
		
		this.reset = function() {
			document.getElementById("id_buttonRP").style.display = "none";
		}
		
		function handleBackToHomeFunction(event) {
			event.preventDefault();
			pageManager.goToHomepage();
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//EXAMRECORD/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	function RecordFetcher() {
		
		this.show = function(_recordID) {
		    var url = 'GetExamRecord?' + '&recordID=' + encodeURIComponent(_recordID);
			var self = this;
			
			document.getElementById("id_recordDetailsSection").style.display = "block";
			document.getElementById("id_examInfoSection").style.display = "block";
			document.getElementById("id_studentInfoSection").style.display = "block";
			
		    sendReq("GET", url, null, function(x) {
		        if (x.readyState === XMLHttpRequest.DONE) {
		            var message = x.responseText;
		            if (x.status === 200) {
		                self.update(message);
		            } else if (x.status === 403) {
		                window.location.href = x.getResponseHeader("Location");
		                window.sessionStorage.removeItem('user');
		            } else {
		               pageManager.showErrorText(message)
		            }
		        }
		    });

		}
			
		this.update = function(_json) {
			var examRecord = JSON.parse(_json);
			
			document.getElementById('id_recordID').textContent = examRecord.recordID;
			document.getElementById('id_recordCreationDate').textContent = examRecord.creationDate;
			document.getElementById('id_recordCreationTime').textContent = examRecord.creationTime;

			document.getElementById('id_recordCourseName').textContent = examRecord.course.name;
			document.getElementById('id_recordExamDate').textContent = examRecord.examDate.date;

			var tbody = document.querySelector('#id_studentInfoSection tbody');
			examRecord.srList.forEach(reg => {
			    var { student, gradeStatus } = reg; // deconstruction of the object
			    var tr = document.createElement('tr');

			    var cell =
					[ student.studentID,
				      student.major,
				      student.name,
				      student.surname,
				      gradeStatus.grade
				    ]
				
				cell.forEach((value, index) => {
			      	var td = document.createElement('td');
					
				 	if(index == 4) {
						if (value === -1) td.textContent = "Absent";
					    else if (value === -2) td.textContent = "Failed";
					    else if (value === -3) td.textContent = "Failed (Deferred)";
					    else if (value === 31) td.textContent = "30 cum laude";
					    else if (value >= 0 && value <= 30) td.textContent = value;
					} else {
						td.textContent = value;
					}
					
			     	tr.appendChild(td);
			    });
				
			    tbody.appendChild(tr);
			  });
		}
		
		this.reset = function() {
			document.getElementById("id_recordDetailsSection").style.display = "none";
			document.getElementById("id_examInfoSection").style.display = "none";
			document.getElementById("id_studentInfoSection").style.display = "none";
			document.querySelector('#id_studentInfoSection tbody').innerHTML = "";
		}
	}
		
	function ButtonsER() {
		
		this.show = function() {
			document.getElementById("id_buttonsERP").style.display = "block";
			
			var goToRecordsButton = document.getElementById("id_goToRecordsButton");
			goToRecordsButton.removeEventListener("click", handleGoToRecordsFunction);
			goToRecordsButton.addEventListener("click", handleGoToRecordsFunction);
			
			var backToHomeButton = document.getElementById("id_backToHomeButton");
			backToHomeButton.removeEventListener("click", handleBackToHomeFunction);
			backToHomeButton.addEventListener("click", handleBackToHomeFunction);
		}
		
		this.reset = function() {
			document.getElementById("id_buttonsERP").style.display = "none";
		}
		
		function handleBackToHomeFunction(event) {
			event.preventDefault();
			pageManager.goToHomepage();
		}
		
		function handleGoToRecordsFunction(event) {
			event.preventDefault();
			pageManager.goToRecords();
		}
	}
		
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//GRADEFORM//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	function StudentInfoFetcher() {
		var examDateID = null;
		var studentID = null;
		var self = this;
		
		this.show = function(_examDateID, _studentID) {
			
			if(_examDateID)
				examDateID = _examDateID;
			if(_studentID)
				studentID = _studentID;
			
			var url = 'GetStudentInfo?' +
	        	'&examDateID=' + encodeURIComponent(examDateID) +
				'&studentID=' + encodeURIComponent(studentID);
						
			sendReq("GET", url, null,
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						if(x.status == 200) {
							self.update(message, studentID);
						} else if(x.status == 403) {
							window.location.href = x.getResponseHeader("Location");
				        	window.sessionStorage.removeItem('user');
						} else {
							pageManager.showErrorText(message)
						}
					}
				}
			);
		}
		
		this.update = function(_json, _studentID) {
		    var student = JSON.parse(_json);
			
		    document.getElementById("id_studentIDGF").textContent = student.studentID;
		    document.getElementById("id_surnameGF").textContent = student.surname;
		    document.getElementById("id_nameGF").textContent = student.name;
		    document.getElementById("id_emailGF").textContent = student.email;
		    document.getElementById("id_majorGF").textContent = student.major;
			
			document.getElementById("id_studentInfoGF").style.display = "block";
			document.getElementById("id_gradeForm").style.display = "block";
			
			var saveButton = document.getElementById("id_sendFormButton");
			saveButton.style.display = "block";
			saveButton.removeEventListener("click", handleSaveFunction);
			saveButton.addEventListener("click", handleSaveFunction);
		}
		
		this.reset = function() {
			document.getElementById("id_studentInfoGF").style.display = "none";
			document.getElementById("id_gradeForm").style.display = "none";
			document.getElementById("id_errorText").style.display = "none";
			document.getElementById("id_sendFormButton").style.display = "none";
		}
		
		function handleSaveFunction(event) {
			event.preventDefault();
		    var form = event.target.closest("form");
		    if (form.checkValidity()) {
		        document.getElementById("id_studentIDInput").value = studentID;
		        document.getElementById("id_examDateIDInput").value = examDateID;
		        sendReq("POST", 'ModifyGrade', event.target.closest("form"),
		            function(x) {
		                if (x.readyState == XMLHttpRequest.DONE) {
		                    var message = x.responseText;
		                    if (x.status == 200) {
		                        pageManager.goToRegisteredStudents(examDateID);
		                    } else if (x.status == 403) {
		                        window.location.href = x.getResponseHeader("Location");
		                        window.sessionStorage.removeItem('user');
		                    } else {
		                        pageManager.showErrorText(message)
		                    }
		                }
		            }
		        );
		    } else {
		        form.reportValidity();
		    }
		}
	}
	
	function ButtonsGF() {
		var examDateID = null;
		
		this.show = function(_examDateID) {
			
			if(_examDateID)
				examDateID = _examDateID;
			
			var backButton = document.getElementById("id_buttonGF");
			
			backButton.style.display = "block";
			
			backButton.removeEventListener("click", handleBackButtonFunction);
			backButton.addEventListener("click", handleBackButtonFunction);
		}
		
		function handleBackButtonFunction(event) {
			event.preventDefault();
			pageManager.goToRegisteredStudents(examDateID);
		}
		
		this.reset = function() {
			document.getElementById("id_buttonGF").style.display = "none";
		}
	}
}