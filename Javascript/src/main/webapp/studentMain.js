/**
 * 
 */

{
	let pageManager = new PageManager();
		
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
			
			var trashIcon = new TrashIcon();
			var popupButtons = new PopupButtons();
			
			var examResultFetcher = new ExamResultFetcher();
			var backHomeButton = new BackHomeButton();
			
			var self = this;
			
	  	    this.start = function() {
				var username = JSON.parse(sessionStorage.getItem('user')).username;
	 			let personalMessage = new PersonalMessage(username, document.getElementById("id_username"));
				personalMessage.show();
				
				document.getElementById("id_errorText").style.display = "none";
				document.getElementById("id_errorText").style.color = "red";
				
				document.querySelector("a[href='Logout']").addEventListener('click', () => {
		        	window.sessionStorage.removeItem('username');
		  		})
				
				self.goToHomepage();
			}
			
			this.fetchDates = function(_courseID) {
				dateFetcher.show(_courseID);
			}
			
			this.enableRefuseButton = function(_examDateID) {
				trashIcon.show(_examDateID);
			}
			
			this.refresh = function() {
				courseFetcher.reset();
				dateFetcher.reset();
				
				trashIcon.reset();
				popupButtons.reset();
				
				examResultFetcher.reset();
				backHomeButton.reset();
			};
			
			this.goToHomepage = function() {
				self.refresh();
				courseFetcher.show();
			}
			
			this.goToExamResult = function(_examDateID) {
				self.refresh();
				examResultFetcher.show(_examDateID);
				backHomeButton.show();
			}
			
			this.goToPopup = function(_examDateID) {
				self.refresh();
				popupButtons.show(_examDateID);
			}
			
			this.showErrorText = function(errorText) {
				document.getElementById("id_errorText").style.display = "block";
				document.getElementById("id_errorText").textContent = errorText;
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
				sendReq("GET", 'GetCoursesStudent', null,
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
	
					a.href = "#"; // Prevents page reload
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
				sendReq("GET", 'GetDatesStudent?courseID=' + encodeURIComponent(_courseID), null,
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
					a.textContent = d.examDate.date;
					a.setAttribute("examDateID", d.examDate.examDateID);
					
					a.addEventListener("click", () => {
						event.preventDefault();
						pageManager.goToExamResult(d.examDate.examDateID);
					});
					
					if(!d.registered) {
						let regButton = document.createElement("button");
						regButton.textContent = "REGISTER";
						regButton.addEventListener("click", e => {
							e.preventDefault();
							let url = 'SubscribeToExam?examDateID=' + d.examDate.examDateID;
							sendReq("GET", url, null,
								function(x) {
									if (x.readyState == XMLHttpRequest.DONE) {
										var message = x.responseText;
										self.reset();
										if(x.status == 200) {
											pageManager.showErrorText("Successfully registered");
										} else if(x.status == 403) {
											window.location.href = x.getResponseHeader("Location");
								        	window.sessionStorage.removeItem('user');
										} else {
											pageManager.showErrorText(message);
										}
									}
								}
							);
						});
						li.appendChild(regButton);
					}
					
					li.prepend(a);
					listElement.appendChild(li);
				});
				
				listElement.style.display = "block";
			}
			
			this.reset = function() {
				document.getElementById("id_errorText").style.display = "none";
				document.getElementById("id_datesList").style.display = "none";
			}
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//EXAM RESULT////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		function ExamResultFetcher() {
			
			var self = this;
			var examDateID = null;
			
			this.show = function(_examDateID) {
				
				if(_examDateID)
					examDateID = _examDateID;
				
				let url = 'GetExamResult?examDateID=' + _examDateID;
				
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
				if(_json !== "not published") {
					var result = JSON.parse(_json);

				    var student = result.student;
				    var course = result.course;
				    var examDate = result.examDate;
				    var grade = result.gradeStatus.grade;

				    
				    document.getElementById("id_studentName").textContent = student.name;
				    document.getElementById("id_studentSurname").textContent = student.surname;
				    document.getElementById("id_studentID").textContent = student.studentID;
				    document.getElementById("id_studentMajor").textContent = student.major;

				    
				    document.getElementById("id_courseName").textContent = course.name;
					document.getElementById("id_courseID").textContent = course.courseID;
				    document.getElementById("id_examDate").textContent = examDate.date;

				    
				    let gradeStr = "None";
				    if (grade === -1) gradeStr = "Absent";
				    else if (grade === -2) gradeStr = "Failed";
				    else if (grade === -3) gradeStr = "Failed (Deferred)";
				    else if (grade === 31) gradeStr = "30 cum laude";
				    else if (grade >= 18 && grade <= 30) gradeStr = grade.toString();
					
					if(result.gradeStatus.status === "refused") {
						document.getElementById("id_trashIcon").style.display = "none";
						pageManager.showErrorText("Grade has been refused");							
					} else {	
						if(grade >= 18 && grade <= 31) pageManager.enableRefuseButton(examDateID);
						else pageManager.showErrorText("Cannot refuse grade");
					}
					
					if(result.gradeStatus.status === "recorded") {
						document.getElementById("id_trashIcon").style.display = "none";
						pageManager.showErrorText("Grade has been recorded");
					}
					
				    document.getElementById("id_examGrade").textContent = gradeStr;
					document.getElementById("id_examResult").style.display = "block";
				} else {
					pageManager.showErrorText("Grade not available");
					document.getElementById("id_trashIcon").style.display = "none";
				}
			}
			
			this.reset = function() {
				document.getElementById("id_examResult").style.display = "none";
			}
		}
		
		function TrashIcon() {
			
			var examDateID = null;
			
			this.show = function(_examDateID) {
	            if(_examDateID)
					examDateID = _examDateID;
	            
	            let examResult = document.getElementById("id_examResult");
	            let trashIcon = document.getElementById("id_trashIcon");
	            
	            examResult.removeEventListener("dragstart", handleDragStartFunction);
	            trashIcon.removeEventListener("dragenter", handleDragEnterFunction);
	            trashIcon.removeEventListener("dragover", handleDragOverFunction);
	            trashIcon.removeEventListener("drop", handleDropFunction);
	            
	            examResult.addEventListener("dragstart", handleDragStartFunction);
	            trashIcon.addEventListener("dragenter", handleDragEnterFunction);
	            trashIcon.addEventListener("dragover", handleDragOverFunction);
	            trashIcon.addEventListener("drop", handleDropFunction);
	            
	            document.getElementById("id_trashIcon").style.display = "block";
	        }
	        
	        this.reset = function() {
	            document.getElementById("id_trashIcon").style.display = "none";
	        }
	        
	        function handleDragStartFunction(event) {
	            event.dataTransfer.setData("text/plain", examDateID);
	            event.dataTransfer.effectAllowed = "move";
	        }
	        
	        function handleDragEnterFunction(event) {
	            event.preventDefault();
	        }
	        
	        function handleDragOverFunction(event) {
	            event.preventDefault();
	            event.dataTransfer.dropEffect = "move";
	        }
	        
	        function handleDropFunction(event) {
	            event.preventDefault();
	            let data = event.dataTransfer.getData("text/plain");
	            pageManager.goToPopup(data);
	        }
			
		}
		
		function PopupButtons() {
			var examDateID = null;
			this.show = function(_examDateID) {
				if(_examDateID)
					examDateID = _examDateID;
				document.getElementById("id_popupButtons").style.display = "block";
				
				var cancelButton = document.getElementById("id_cancelButton");
				cancelButton.removeEventListener("click", handleCancelFunction)
				cancelButton.addEventListener("click", handleCancelFunction)
				
				var confirmButton = document.getElementById("id_confirmButton");
				confirmButton.removeEventListener("click", handleConfirmFunction)
				confirmButton.addEventListener("click", handleConfirmFunction)
			}
			
			this.reset = function() {
				document.getElementById("id_popupButtons").style.display = "none";
			}
			
			function handleCancelFunction(event) {
				event.preventDefault();
				pageManager.goToExamResult(examDateID);
			}
			
			function handleConfirmFunction(event) {
				event.preventDefault();
				var url = 'RefuseGrade?examDateID=' + examDateID;
				sendReq("POST", url, null,
					function(x) {
						if (x.readyState == XMLHttpRequest.DONE) {
							var message = x.responseText;
							if(x.status == 200) {
								pageManager.goToExamResult(examDateID);
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
		}
		
		function BackHomeButton() {
			
			this.show = function () {
				var backButton = document.getElementById("id_backToHomeButton");
				
				backButton.style.display = "block";
				backButton.removeEventListener("click", handleClickFunction)
				backButton.addEventListener("click", handleClickFunction)
			}
			
			this.reset = function() {
				document.getElementById("id_backToHomeButton").style.display = "none";
			}
			
			function handleClickFunction(event) {
				event.preventDefault();
				pageManager.goToHomepage();
			}
		}
}