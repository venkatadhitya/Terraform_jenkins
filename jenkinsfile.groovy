pipeline {
    parameters{
        booleanParam(name:'autoAppropve', defaultValue: false, description: 'Automatically run apply after generating paln?')
    }
  

  environment {
    AWS_ACCESS_KEY_ID     = credentials('aws-access-key-id')
    AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
  }

  agent any
  stages {
    stage('Checkout') {
      steps {
        script{
            dir("DEMOEC2")
                {
                    git 'https://github.com/venkatadhitya/demoec2.git'

                }
            }   
       }
    }
  }
    stage('plan'){
        steps{
            sh 'pwd;cd DEMOEC2/ ; terraform init'
            sh "pwd;cd DEMOEC2/ ; terraform plan -out tfplan"
            sh 'pwd;cd DEMOEC2/ ; terraform show -no-color tfplan >tfplan.txt'
        }
    }
    stage('Approval'){
        when {
            not {
                equals expected: true, actual: params.autoAppropve
            }
        }
    
    steps{
        script{
            def plan = readfile 'DEMOEC2/tfplan.txt'
            input message: "do you want to apply the plan?",
            parameters: [text(name: 'plan', description: 'please review the plan', defaultValue: plan)]
        }
    }
    }
    stage('Apply'){
        steps{
            sh "pwd;cd DEMOEC2/ ; terraform apply -input=false tfplan"
        }
    }
}
