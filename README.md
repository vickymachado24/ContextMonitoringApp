# ContextMonitoringApp
Android app for context monitoring

Q1. Specifications to Provide to Health-Dev for Ideal Code Generation
If new to programming and would want to use the Health-Dev framework to build a context-sensing application, 
one should follow the below specifications to ensure that the framework generates ideal code:

Sensor Specifications: Health-Dev requires detailed sensor specifications:

   Specify accelerometer data for respiratory rate sensing and back camera along with the flash for heart rate sensing.
   Provide the data processing requirements for each sensor (e.g., calculate heart rate from color variation in video frames, 
   calculate respiratory rate from accelerometer data).
   Including communication protocols for sensors (Bluetooth or ZigBee, based on how data would be transmitted).
   Signal Processing Algorithms: We would need to specify algorithms used for processing sensor data, such as:
      Respiratory rate: Calculation from accelerometer data using signal processing techniques.
      Heart rate: Calculating from video frames using color intensity changes.
   UI Specifications: Defining the user interface requirements, such as buttons to start/stop to collect data (e.g., “Measure Heart Rate”, “Measure Respiratory Rate” buttons) 
   and displaying the values collected/ updated values on-screen. Health-Dev supports code generation for Android, 
   so specify the necessary elements like buttons, sliders, text views, and graphs.
   Data Storage and Feedback: Specify what needs to be stored  (heart rate, respiratory rate, symptom ratings) locally using RoomDB or another storage mechanism(cloud storage also).
   We would also need a method to retrieve and display this data, which can be achieved by a dao interface.

By providing these specifications, Health-Dev can help generate code for sensors, processing algorithms, communication protocols, and user interfaces(healthDev).

Q2. Providing Feedback Using the bHealthy Suite
Enabling feedback and enhancing the context-sensing experience, using concepts of the bHealthy application suite, 
by incorporating personalized wellness reports and recommendations on activity within the application:

   Feedback Loop: Like bHealthy, we can also provide wellness reports to the user based on his/her previous or current health data. 
   That would include monitoring trending heart rate, respiratory rate, and symptom data, and analysing the data before providing any insight such 
   as: "Your heart rate is high consistently, consider taking some time to relax" or "Your respiratory rate is improving, keep up the work."

   User Engagement: We can add some features of gamification or incentives in order to enhance user compliance. For example, We can provide challenges or goals like, 
   "Keep your heart rate below a certain threshold for the next few days." We may also give suggestions regarding calming activities or mindfulness exercises 
   using real-time heart rate, similar to how bHealthy achieves this with neurofeedback to encourage healthy behaviors(bhealth-1).

   Novelty in Context Sensing: An underlying novelty can help to provide feedback that varies with varying context like  notifying users when their respiration signals 
   high stress during certain times of the day or week. We can incorporate environmental sensors that provide changes in feedback, such as poor air quality, and 
   also provide recommendation for not going out or alerting about any future activity.

   Coupled with real-world data, these feedback loops have the potential to increase user engagement while providing actionable insights into their health trends.

Q3. Views on Mobile Computing After Project 1
   Yes, my views on mobile computing have changed after completing Project 1 and reading both papers. I now see mobile computing not just  just app development— but it's about contextual data sensing, 
   real-time data processing, analysis and adaptive context based feedback.

   Beyond Apps: Mobile computing involves integrating hardware via sensors and software (data processing algorithms) to collect, process, and analyze real-time data. 
   In Health-Dev, the focus on body sensor networks and auto code generation depicts that mobile computing is about creating systems 
   that easily adapt to users' dynamic environments, enabling reliability and efficiency. The complex data collection from sensors (heart rate via camera, respiratory rate via accelerometer) in 
   the project illustrates this well.

   Real-time Health Monitoring: Mobile computing is increasingly about real-time monitoring and feedback, as seen in bHealthy. 
   Mobile apps need to engage users, process large amounts of data (like video frames or sensor data), and generate useful insights on the go for the user. 
   The project, which calculates heart and respiratory rates and logs symptoms, reflects this real-time data processing aspect.

   In conclusion, mobile computing now encompasses sensor integration, energy efficiency, and real-time health data analysis, 
   making it a much broader and more impactful field than just app development.

