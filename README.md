# streamscale

This project has been implemented by WS-19/20 BDAPro students:
- Ioannis Prapas
- Ankush Sharma
- Sokratis Papadopoulos

Supervised by Ankit Chaudhary

### Project description
Autoscale streaming jobs: Use minimum resources but guarantee optimal execution.

`utils/` : Contains utility bash scripts that run cluster start flink and kafka and set up the environment variables (bash)

`ml_scaler/`: Control agent that implements Q-learning and controls running flink jobs (python3)

`flink_queries/`: Stream queries tested (Java)

`experimental_results`: Jupyter notebook to generate plots to show results (Python)

`datagen/`: Data generators that produce data and send them to kafka (Java)

