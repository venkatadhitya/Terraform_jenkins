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
                    dir("Terraform")
                        {
                            git 'https://github.com/venkatadhitya/Terraform_jenkins.git'

                        }
                    }   
                }
            }
        }
    stage ('plan'){
        steps{
            sh 'pwd;cd Terraform/ ; terraform init'
            sh "pwd;cd Terraform/ ; terraform plan -out tfplan"
            sh 'pwd;cd Terraform/ ; terraform show -no-color tfplan >tfplan.txt'
        }
    }
    stage ('Approval'){
        when {
            not {
                equals expected: true, actual: params.autoAppropve
            }
        }
    
        steps{
            script{
                def plan = readfile 'Terraform/tfplan.txt'
                input message: "do you want to apply the plan?",
                parameters: [text(name: 'plan', description: 'please review the plan', defaultValue: plan)]
            }
        }
    }
    stage ('Apply'){
        steps{
            sh "pwd;cd Terraform/ ; terraform apply -input=false tfplan"
        }
    }
}
